/**  
 * Title CacheManagerState.java  
 * Description  
 * @author danyuan
 * @date Nov 13, 2020
 * @version 1.0.0
 * site: pddon.cn
 */
package com.pddon.framework.easyapi.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CacheManagerState implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private String cacheName;
	private long hitCount;
	private long missCount;
	private long loadSuccessCount;
	private long loadTotalCount;
	private long totalLoadTime;
	private long evictionCount;
}
