/**  
* Title BeanPropertyUtilTest.java  
* Description  
* @author danyuan
* @date Nov 14, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.easyapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.pddon.framework.easyapi.dto.ComplextResponse;
import org.junit.Test;

import com.pddon.framework.easyapi.dto.ComplextRequest;
import com.pddon.framework.easyapi.dto.SkuCurrency;
import com.pddon.framework.easyapi.dto.SkuItem;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;

public class BeanPropertyUtilTest {

	@Test
	public void testRequestToStringMap(){
		System.out.println("###################testRequestToStringMap######################");
		ComplextRequest req = new ComplextRequest();
		List<SkuCurrency> priceInfos = new ArrayList<>();
		priceInfos.add(new SkuCurrency("CNY",new BigDecimal(3.6).setScale(2,BigDecimal.ROUND_HALF_UP)));
		priceInfos.add(new SkuCurrency("USD",new BigDecimal(0.56).setScale(2,BigDecimal.ROUND_HALF_UP)));
		List<SkuItem> skuList = new ArrayList<>();
		skuList.add(new SkuItem(1,"skuName1", new BigDecimal(3.6).setScale(2,BigDecimal.ROUND_HALF_UP),priceInfos));
		skuList.add(new SkuItem(3,"skuName2", new BigDecimal(88.2).setScale(2,BigDecimal.ROUND_HALF_UP),priceInfos));
		Map<String, SkuItem> skuTagMap = new HashMap<>();
		skuTagMap.put("goodSku 1", new SkuItem(6,"skuName6", new BigDecimal(22.2).setScale(2,BigDecimal.ROUND_HALF_UP),priceInfos));
		skuTagMap.put("goodSku 2", new SkuItem(8,"skuName8", new BigDecimal(33).setScale(2,BigDecimal.ROUND_HALF_UP),priceInfos));
		String[] introArr = new String[]{"intro test 1","intro test 2","intro test 3"};
		req.setIntroArr(introArr);
		req.setSkuList(skuList);
		req.setSkuTagMap(skuTagMap);
		req.setTotalSku(4);
		req.setDesc("这是一个简单的测试!");
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(req, "", IgnoreSign.class).forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testSkuItemToStringMap(){	
		System.out.println("###################testSkuItemToStringMap######################");
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(new SkuItem(1,"skuName1", new BigDecimal(3.6).setScale(2,BigDecimal.ROUND_HALF_UP),null), "").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testListToStringMap(){
		System.out.println("###################testListToStringMap######################");		
		List<SkuItem> skuList = new ArrayList<>();
		skuList.add(new SkuItem(1,"skuName1", new BigDecimal(3.6).setScale(2,BigDecimal.ROUND_HALF_UP),null));
		skuList.add(new SkuItem(3,"skuName2", new BigDecimal(88.2).setScale(2,BigDecimal.ROUND_HALF_UP),null));
		
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(skuList, "req").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testObjectMapToStringMap(){
		System.out.println("###################testObjectMapToStringMap######################");
		Map<String, SkuItem> skuTagMap = new HashMap<>();
		skuTagMap.put("goodSku 1", new SkuItem(6,"skuName6", new BigDecimal(22.2).setScale(2,BigDecimal.ROUND_HALF_UP),null));
		skuTagMap.put("goodSku 2", new SkuItem(8,"skuName8", new BigDecimal(33).setScale(2,BigDecimal.ROUND_HALF_UP),null));
		
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(skuTagMap, "").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testBaseTypeMapToStringMap(){
		System.out.println("###################testBaseTypeMapToStringMap######################");
		Map<String, String> skuTagMap = new HashMap<>();
		skuTagMap.put("tom", "like fish");
		skuTagMap.put("Joe", "like potato");
		
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(skuTagMap, "").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testNullPorpertiesResponseToStringMap(){
		System.out.println("###################testNullPorpertiesResponseToStringMap######################");
		ComplextResponse resp = new ComplextResponse();
		resp.setDesc("test desc !");
		resp.setSex(true);
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(resp, "").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
	
	@Test
	public void testBaseTypeToStringMap(){
		System.out.println("###################testBaseTypeToStringMap######################");
		boolean sex = true;
		final AtomicInteger i = new AtomicInteger(0);
		BeanPropertyUtil.objToStringMap(sex, "sex").forEach((key,val) -> {
			System.out.println("第"+ i.addAndGet(1) +"行："+key+"="+val);
		});
	}
}
