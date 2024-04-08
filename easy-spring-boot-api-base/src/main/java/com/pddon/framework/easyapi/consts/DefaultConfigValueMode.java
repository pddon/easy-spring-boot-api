/**  
* Title DefaultConfigValueUseType.java  
* Description  
* @author danyuan
* @date Dec 27, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi.consts;

public enum DefaultConfigValueMode {
	APPEND,//添加到后面
	PREPEND,//添加到前面
	OPTION,//默认值，可被配置覆盖
	FORCE_DEFAULT//强制使用默认值
}
