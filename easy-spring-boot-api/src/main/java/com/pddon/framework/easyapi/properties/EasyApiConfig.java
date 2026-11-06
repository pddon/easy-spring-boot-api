/**  
* Title EasyApiConfig.java
* Description  
* @author danyuan
* @date Nov 7, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.properties;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.pddon.framework.easyapi.utils.ClassOriginCheckUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "easyapi")
public class EasyApiConfig implements Serializable {

	/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 是否启用EasyAPi框架
	 */
	private Boolean enable;
	/**
	 * 是否开启swagger
	 */
	private Boolean enableSwagger;
	/**
	 * 默认语言
	 */
	private String locale;
	/**
	 * 是否开启多语言翻译缓存
	 */
	private Boolean enableLanguageCache;
	/**
	 * 多语言缓存的最长时间，单位秒
	 */
	private Long languageCacheSeconds;
	/**
	 * 当未找到该语言的翻译时，是否使用默认翻译内容
	 */
	private Boolean alwaysUseDefaultLocale;
	/**
	 * 防重复提交码过期时间
	 */
	private Long noSubmitRepeatTimeoutSeconds;
	/**
	 * 会话失效时间
	 */
	private Long sessionExpireSeconds;
	/**
	 * 是否强制对所有api响应添加响应壳
	 */
	private Boolean forceAutoAddResponseWrapper;
	/**
	 * 根包名集合,以逗号分隔
	 */
	private String basePackages;
	/**
	 * 是否在控制台打印所有配置信息
	 */
	private Boolean printAllProperties;
	/**
	 * 禁用方法缓存
	 */
	private Boolean disableMethodCache;
	/**
	 * 是否缓存HTTP消息转换器列表。
	 * 默认false，保持原有行为：每次按需重新构建默认消息转换器；
	 * 开启后同一配置实例内复用同一份转换器列表，降低启动期重复构建开销。
	 */
	private Boolean enableHttpMessageConverterCache;
	/**
	 * 根包名集合的本地缓存，避免每次请求重复构造集合与查找启动类包名。
	 * 不参与equals/hashCode/toString，保持新增缓存字段前的对象语义
	 */
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private transient volatile Set<String> allBasePackagesCache;
	/**
	 * 防重复提交相关配置
	 */
	private NoRepeatSubmit noRepeatSubmit = new NoRepeatSubmit();
	/**
	 * 接口验签相关配置
	 */
	private Sign sign = new Sign();
	/**
	 * 分布式状态存储配置
	 */
	private StateStore stateStore = new StateStore();
	/**
	 * 方法结果缓存配置
	 */
	private MethodCache methodCache = new MethodCache();

	/**
	 * 方法结果缓存配置项，对应配置前缀easyapi.method-cache
	 */
	@Data
	public static class MethodCache {
		/**
		 * 按缓存key前缀覆盖注解上的expireSeconds，key为前缀（如User:Perms、User:DataPerms），value为秒数。
		 * 默认空Map，完全沿用注解值，保持原有TTL不变。
		 * 示例（YAML中key含冒号需用方括号引用）：
		 * easyapi.method-cache.expire-seconds-by-prefix:
		 *   "[User:Perms]": 600
		 */
		private Map<String, Long> expireSecondsByPrefix = new HashMap<>();
		/**
		 * 自动参数缓存key是否启用新格式，对应配置项easyapi.method-cache.auto-key-v2，默认false。
		 *
		 * 旧格式（false）保持原有行为：参数值用"-"直接拼接，null参数被省略，
		 * 存在("x-y","z")与("x","y-z")、("x",null)与(null,"x")碰撞，以及字段名子串误剔除的风险。
		 * 新格式（true）：参数值URL编码后用"&"拼接并加"v2"标记，null参数显式占位，
		 * 按字段名精确剔除系统参数，避免上述碰撞。
		 *
		 * 切换代价：开启后旧格式的key全部失效，存量缓存首次请求会回源重建；
		 * 回滚方式：改回false即可，旧格式key及其历史缓存值仍然有效（未过期时直接复用）。
		 */
		private boolean autoKeyV2 = false;
	}

	/**
	 * 分布式状态存储配置项，对应配置前缀easyapi.state-store
	 */
	@Data
	public static class StateStore {
		/**
		 * 存储类型：auto（默认，存在Redis时用Redis，否则本地）、local（强制本地）、redis（强制Redis，缺少Redis时会启动失败）
		 */
		private String type = "auto";
		/**
		 * 是否启用Redis存储装配，对应配置项easyapi.state-store.redis-enabled，默认true（保持原有行为）。
		 *
		 * 仅在classpath存在StringRedisTemplate时才会装配Redis存储；当classpath上有spring-data-redis
		 * 但本应用不需要分布式状态（或启动期需要尽可能少的基础设施）时，设为false即可完全不装配Redis存储，
		 * 此时无论type取值如何都使用本地存储（type=redis除外，仍会因缺少Redis存储而启动失败）。
		 */
		private boolean redisEnabled = true;
		/**
		 * key全局前缀。为空时自动使用"easyapi:<spring.application.name或default>:"，避免多应用共享同一Redis时key冲突
		 */
		private String keyPrefix = "";
		/**
		 * Redis异常时是否回退本地存储。默认true；false为fail-fast（异常直接抛出）
		 */
		private boolean fallbackToLocalOnFailure = true;
		/**
		 * 降级WARN日志的打印间隔，单位秒，默认60秒，避免故障期间刷爆日志
		 */
		private long fallbackWarnIntervalSeconds = 60L;
	}

	/**
	 * 防重复提交配置项，对应配置前缀easyapi.no-repeat-submit
	 */
	@Data
	public static class NoRepeatSubmit {
		/**
		 * 请求处理失败时是否释放防重复提交码。
		 * 默认true：只有业务真正成功才标记已提交，失败（参数校验、验签、业务异常、系统异常）
		 * 会释放提交码，允许客户端纠正后立即重试。
		 * 设为false可回退到历史行为：无论成功失败都标记为已提交。
		 */
		private boolean releaseOnFailure = true;
	}

	/**
	 * 验签配置项，对应配置前缀easyapi.sign
	 */
	@Data
	public static class Sign {
		/**
		 * 请求防重放配置，对应配置前缀easyapi.sign.replay-protection
		 */
		private ReplayProtection replayProtection = new ReplayProtection();

		/**
		 * 验签防重放配置
		 */
		@Data
		public static class ReplayProtection {
			/**
			 * 是否开启时间窗校验与nonce一次性校验。默认false，保持原有验签行为不变
			 */
			private boolean enabled = false;
			/**
			 * 时间戳允许的最大偏差，单位秒，默认300秒
			 */
			private long timestampWindowSeconds = 300L;
			/**
			 * nonce在本地缓存中的保留时间，单位秒，默认300秒
			 */
			private long nonceTtlSeconds = 300L;
			/**
			 * nonce本地缓存的最大条目数，避免内存无界增长，默认100000
			 */
			private int nonceCacheSize = 100000;
			/**
			 * nonce参数名，客户端需以该参数名传递nonce，并参与签名
			 */
			private String nonceParam = "nonce";
			/**
			 * nonce允许的最大长度，超长直接拒绝，避免超长字符串占用缓存
			 */
			private int nonceMaxLength = 128;
		}
	}

	public EasyApiConfig(){
		this.enable = true;
		this.enableSwagger = true;
		this.locale = Locale.CHINA.toString();
		this.enableLanguageCache = true;
		this.languageCacheSeconds = 180L;//默认缓存3分钟
		this.noSubmitRepeatTimeoutSeconds = 120L;//默认2分钟
		this.sessionExpireSeconds = 600L; //默认10分钟
		this.alwaysUseDefaultLocale = true;
		this.forceAutoAddResponseWrapper = true;
		this.basePackages = "";
		this.printAllProperties = false;
		this.disableMethodCache = false;
		this.enableHttpMessageConverterCache = false;
	}
	
	public Set<String> getAllBasePackages(){
		Set<String> cached = this.allBasePackagesCache;
		if(cached != null){
			return cached;
		}
		Set<String> set = new HashSet<>();
		if(!StringUtils.isEmpty(this.basePackages)){
			String[] pkgs = this.basePackages.split(",");
			for(String pkg : pkgs){
				set.add(pkg);
			}
		}
		//添加启动类所在的包
		String defaultBasePackage = ClassOriginCheckUtil.getDeafultBasePackage();
		set.add(defaultBasePackage);
		if(!defaultBasePackage.isEmpty()){
			//启动类包名有效时缓存结果；容器尚未就绪时不缓存，以便后续重试
			set = Collections.unmodifiableSet(set);
			this.allBasePackagesCache = set;
		}
		return set;
	}

	/**
	 * basePackages配置变更时失效本地缓存，保持原有setter语义
	 */
	public EasyApiConfig setBasePackages(String basePackages){
		this.basePackages = basePackages;
		this.allBasePackagesCache = null;
		return this;
	}
	
}
