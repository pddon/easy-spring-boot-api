/**  
* Title CheckRepeatManager.java  
* Description  防重复提交码
* 针对需要防止客户端误操作将需要更新的数据重复提交多次，导致服务器数据异常，主要用于数据插入防重操作。
* 操作方式：客户端生成一个全局唯一防重复提交码，可以使用数字签名字段或者其他全局唯一标识该次提交数据的信息。
* @author danyuan
* @date Nov 8, 2018
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

public interface CheckRepeatManager {

	/**
	 * 校验防重复提交码是否正确
	 * @param key 防重码键值, 用户ID+时间戳 或者 sign值
	 * @throws BusinessException
	 */
	void check(String key);
	/**
	 * 提交失败后，主动释放防重复码
	 * @author danyuan
	 */
	void release(String key);
	/**
	 * 标记防重复提交码已经被成功使用
	 * @author danyuan
	 */
	void used(String key);
}
