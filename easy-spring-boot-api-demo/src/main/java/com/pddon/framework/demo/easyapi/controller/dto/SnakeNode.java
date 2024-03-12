/**  
* Title SnakeNode.java  
* Description  贪吃蛇节点信息
* @author danyuan
* @date Nov 29, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SnakeNode implements Serializable{/** 
	 *serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 下一个节点
	 */
	private SnakeNode next;
	
	/**
	 * 此段下标
	 */
	private Integer index;
	/**
	 * 此段身体长度
	 */
	private Integer len;
	/**
	 * 此段颜色
	 */
	private String color;
	/**
	 * 描述信息
	 */
	private String desc;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		result = prime * result + ((len == null) ? 0 : len.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SnakeNode other = (SnakeNode) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (len == null) {
			if (other.len != null)
				return false;
		} else if (!len.equals(other.len))
			return false;
		if (next == null) {
			if (other.next != null)
				return false;
		} else if (!next.equals(other.next))
			return false;
		
		return true;
	}
	
}
