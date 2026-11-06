/**
* Title RedisStateCacheManager.java
* Description 基于分布式状态存储的CacheManager实现：接入Redis后会话、方法缓存、国际化缓存、验证码等跨实例共享
* @author pddon
* @date 2026-11-05
* @version 1.0.0
* site: pddon.cn
*/
package com.pddon.framework.easyapi.cache;

import java.util.Collections;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.dto.CacheManagerState;
import com.pddon.framework.easyapi.state.DistributedStateStore;

/**
 * 分布式缓存管理器：所有值以JSON存储到统一状态存储（本地实现或Redis实现）。
 * <p>key规范："cache:write:业务key" / "cache:read:业务key"，最终由状态存储追加全局前缀
 * （默认easyapi:应用名:），因此Redis中的key形如 easyapi:app:cache:write:Session:xxx，可直接排查。</p>
 * <p>值规范：JSON信封 {"c":"原始类名","v":值JSON}，不使用JDK序列化，避免类版本耦合；
 * 读取时按调用方传入的Class反序列化，调用方未传Class时（已废弃接口）返回JSON原生结构，
 * 但String/Number/Boolean/Date等简单类型会还原为原始类型。</p>
 * <p>与本地实现的差异（Redis模式）：</p>
 * <p>1、本地实现按expireSeconds维护多个缓存桶；Redis的TTL是key级别，因此expireSeconds只作为
 * 写入/续期参数，读取和删除按key生效（同一个key在不同调用点应使用相同的TTL）；</p>
 * <p>2、clear(expireSeconds)无法只清理某个TTL桶，会清理整个cache命名空间（clear()语义）；</p>
 * <p>3、stats/getAllCaches/getCaches无法在不阻塞Redis的前提下枚举全部key，返回空集合。</p>
 */
@Slf4j
public class RedisStateCacheManager implements CacheManager {

	private static final String KEY_NAMESPACE = "cache:";

	private static final String KEY_WRITE = KEY_NAMESPACE + "write:";

	private static final String KEY_READ = KEY_NAMESPACE + "read:";

	private static final long DEFAULT_EXPIRE_SECONDS = 180L;

	private static final String FIELD_TYPE = "c";

	private static final String FIELD_VALUE = "v";

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

	private final DistributedStateStore stateStore;

	public RedisStateCacheManager(DistributedStateStore stateStore) {
		this.stateStore = stateStore;
	}

	private String namespace(CacheExpireMode mode) {
		return CacheExpireMode.EXPIRE_AFTER_WRITE.equals(mode) ? KEY_WRITE : KEY_READ;
	}

	private String cacheKey(String key, CacheExpireMode mode) {
		return namespace(mode) + key;
	}

	private long ttl(Long expireSeconds) {
		return expireSeconds == null || expireSeconds <= 0 ? DEFAULT_EXPIRE_SECONDS : expireSeconds;
	}

	private static boolean isSimpleType(Class<?> type) {
		return type == String.class || type == Boolean.class || type == Character.class
				|| type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
				|| type == Float.class || type == Double.class || type == java.math.BigInteger.class
				|| type == java.math.BigDecimal.class || type == java.util.Date.class;
	}

	private static Class<?> resolveClass(String className) {
		if (className == null || className.isEmpty()) {
			return null;
		}
		try {
			return Class.forName(className);
		} catch (Throwable e) {
			return null;
		}
	}

	private String serialize(Object value) {
		try {
			ObjectNode envelope = MAPPER.createObjectNode();
			envelope.put(FIELD_TYPE, value.getClass().getName());
			envelope.set(FIELD_VALUE, MAPPER.valueToTree(value));
			return MAPPER.writeValueAsString(envelope);
		} catch (Exception e) {
			log.warn("缓存序列化失败，跳过本次缓存，type={}, cause={}", value.getClass().getName(), e.getMessage());
			return null;
		}
	}

	private JsonNode readEnvelope(String raw, String key) {
		if (raw == null) {
			return null;
		}
		try {
			JsonNode node = MAPPER.readTree(raw);
			JsonNode valueNode = node == null ? null : node.get(FIELD_VALUE);
			if (valueNode == null || valueNode.isNull()) {
				return null;
			}
			return node;
		} catch (Exception e) {
			log.warn("缓存反序列化失败，按未命中处理，key={}, cause={}", key, e.getMessage());
			return null;
		}
	}

	@Override
	public void set(String key, Object value, Long expireSeconds) {
		this.set(key, value, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public void set(String key, Object value, Long expireSeconds, CacheExpireMode mode) {
		this.set(key, value, expireSeconds, null, mode);
	}

	@Override
	public void set(String key, Object value, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
		if (key == null || value == null) {
			return;
		}
		//oldExpireSeconds用于本地实现清理旧TTL桶；Redis为key级TTL，直接覆盖写入即可
		String raw = serialize(value);
		if (raw == null) {
			return;
		}
		stateStore.set(cacheKey(key, mode), raw, ttl(expireSeconds));
	}

	@Override
	public boolean exists(String key) {
		return this.exists(key, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public boolean exists(String key, Long expireSeconds) {
		return this.exists(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public boolean exists(String key, Long expireSeconds, CacheExpireMode mode) {
		if (key == null) {
			return false;
		}
		return stateStore.exists(cacheKey(key, mode), ttl(expireSeconds));
	}

	@Override
	public boolean exists(String key, CacheExpireMode mode) {
		return this.exists(key, null, mode);
	}

	@Override
	public Object get(String key, Long expireSeconds, CacheExpireMode mode) {
		if (key == null) {
			return null;
		}
		String fullKey = cacheKey(key, mode);
		JsonNode node = readEnvelope(stateStore.get(fullKey, ttl(expireSeconds)), fullKey);
		if (node == null) {
			return null;
		}
		JsonNode valueNode = node.get(FIELD_VALUE);
		Class<?> storedType = resolveClass(node.path(FIELD_TYPE).asText(null));
		try {
			if (storedType != null && isSimpleType(storedType)) {
				return MAPPER.convertValue(valueNode, storedType);
			}
			//未提供目标类型时返回JSON原生结构（Map/List/标量），与本地实现"原对象"不同，仅供管理排查
			return MAPPER.convertValue(valueNode, Object.class);
		} catch (Exception e) {
			log.warn("缓存值转换失败，按未命中处理，key={}, cause={}", fullKey, e.getMessage());
			return null;
		}
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		return this.get(key, type, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public <T> T get(String key, Class<T> type, Long expireSeconds) {
		return this.get(key, type, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public <T> T get(String key, Class<T> type, CacheExpireMode mode) {
		return this.get(key, type, null, mode);
	}

	@Override
	public <T> T get(String key, Class<T> type, Long expireSeconds, CacheExpireMode mode) {
		if (key == null || type == null) {
			return null;
		}
		String fullKey = cacheKey(key, mode);
		JsonNode node = readEnvelope(stateStore.get(fullKey, ttl(expireSeconds)), fullKey);
		if (node == null) {
			return null;
		}
		try {
			return MAPPER.convertValue(node.get(FIELD_VALUE), type);
		} catch (Exception e) {
			log.warn("缓存反序列化失败，按未命中处理，key={}, type={}, cause={}", fullKey, type.getName(), e.getMessage());
			return null;
		}
	}

	@Override
	public void setExpire(String key, Long expireSeconds) {
		this.setExpire(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds) {
		this.setExpire(key, expireSeconds, oldExpireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public void setExpire(String key, Long expireSeconds, CacheExpireMode mode) {
		this.setExpire(key, expireSeconds, null, mode);
	}

	@Override
	public void setExpire(String key, Long expireSeconds, Long oldExpireSeconds, CacheExpireMode mode) {
		if (key == null) {
			return;
		}
		//只修改TTL，不重写值，避免会话续期时覆盖并发更新
		stateStore.expire(cacheKey(key, mode), ttl(expireSeconds));
	}

	@Override
	public void remove(String key) {
		this.remove(key, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public void remove(String key, Long expireSeconds) {
		this.remove(key, expireSeconds, CacheExpireMode.EXPIRE_AFTER_WRITE);
	}

	@Override
	public void remove(String key, Long expireSeconds, CacheExpireMode mode) {
		if (key == null) {
			return;
		}
		stateStore.delete(cacheKey(key, mode), ttl(expireSeconds));
	}

	@Override
	public void remove(String key, CacheExpireMode mode) {
		this.remove(key, null, mode);
	}

	@Override
	public void clear() {
		stateStore.deleteByPrefix(KEY_NAMESPACE);
	}

	@Override
	public void clear(Long expireSeconds) {
		//Redis无法按TTL桶清理，退化为清理整个cache命名空间
		stateStore.deleteByPrefix(KEY_NAMESPACE);
	}

	@Override
	public Map<String, CacheManagerState> stats(CacheExpireMode mode) {
		//枚举全部key需要SCAN甚至KEYS，代价高且可能阻塞，Redis模式不提供本地统计
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Map<String, Object>> getAllCaches(CacheExpireMode mode) {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getCaches(Long expireSeconds, CacheExpireMode mode) {
		return Collections.emptyMap();
	}
}
