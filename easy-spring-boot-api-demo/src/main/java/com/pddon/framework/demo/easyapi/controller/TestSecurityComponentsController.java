/**  
* Title TestSecurityComponentsController.java  
* Description  测试接口安全组件功能
* @author danyuan
* @date Nov 29, 2020
* @version 1.0.0
* site: pddon.cn
*/ 
package com.pddon.framework.demo.easyapi.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pddon.framework.demo.easyapi.controller.dto.AccountInfo;
import com.pddon.framework.demo.easyapi.controller.dto.SnakeNode;
import com.pddon.framework.demo.easyapi.controller.dto.UserInfoDto;
import com.pddon.framework.demo.easyapi.controller.request.SignTestRequest;
import com.pddon.framework.demo.easyapi.controller.response.SignTestResponse;
import com.pddon.framework.easyapi.annotation.Decrypt;
import com.pddon.framework.easyapi.annotation.Encrypt;
import com.pddon.framework.easyapi.annotation.RequiredNoRepeatSubmit;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.consts.DenyRepeatSubmitType;
import com.pddon.framework.easyapi.consts.SignScope;

@Api(tags = "测试接口安全组件功能")
@RestController
@Slf4j
public class TestSecurityComponentsController {

	@PostMapping(value="testSign",
			name="测试请求响应加签验签功能")
	@ApiOperation(value="测试请求响应加签验签功能", notes="测试请求响应加签验签功能")
	@RequiredSign(scope = SignScope.BOTH)
	public SignTestResponse testSign(
			@RequestBody SignTestRequest req, String note, BigDecimal amount, @RequestParam("mobilePhone") String phone
			) {
		SignTestResponse resp = new SignTestResponse();
		AccountInfo account = new AccountInfo();
		account.setAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP));
		account.setFreezeAmount(new BigDecimal(2856).setScale(2, BigDecimal.ROUND_HALF_UP));
		account.setNote(note);
		resp.setPhone(phone);
		resp.setAccount(account);
		resp.setDesc("test response !");
		resp.setTest("校验后响应也不能为空哦!");
		resp.setAge(26);
		return resp;
	}
	
	@GetMapping(value="testCircleDependentSign",
			name="测试循环依赖响应加签功能")
	@ApiOperation(value="测试循环依赖响应加签功能", notes="测试循环依赖响应加签功能")
	@RequiredSign(scope = SignScope.RESPONSE)
	public SnakeNode testCircleDependentSign() {
		SnakeNode node1 = new SnakeNode();
		node1.setColor("red");
		node1.setDesc("这是第一节身体!");
		node1.setIndex(1);
		node1.setLen(50);
		SnakeNode node2 = new SnakeNode();
		node2.setColor("blue");
		node2.setDesc("这是第二节身体!");
		node2.setIndex(2);
		node2.setLen(100);
		SnakeNode node3 = new SnakeNode();
		node3.setColor("black");
		node3.setDesc("这是第三节身体!");
		node3.setIndex(3);
		node3.setLen(10);
		node1.setNext(node2);
		node2.setNext(node3);
		return node1;
	}
	
	@GetMapping(value="testDecryptParamAndResponse",
			name="测试加解密普通参数和响应信息")
	@ApiOperation(value="测试加解密普通参数和响应信息", notes="测试加解密普通参数和响应信息")
	@Encrypt
	public UserInfoDto testDecryptParamAndResponse(@RequestParam("desc") @Decrypt String desc) {
		UserInfoDto user = new UserInfoDto();
		user.setUsername("danyuan");
		user.setAge(18);
		user.setDesc("this is a encrypt test .");		
		return user;
	}
	
	@GetMapping(value="testEncryptListResponseData",
			name="测试加密列表响应信息")
	@ApiOperation(value="测试加密列表响应信息", notes="测试加密列表响应信息")
	public List<UserInfoDto> testEncryptListResponseData(){
		List<UserInfoDto> list = new ArrayList<>();
		UserInfoDto user1 = new UserInfoDto();
		user1.setUsername("danyuan");
		user1.setAge(18);
		user1.setDesc("this is a encrypt test 1 .");
		UserInfoDto user2 = new UserInfoDto();
		user2.setUsername("小明");
		user2.setAge(22);
		user2.setDesc("this is a encrypt test 2 .");
		list.add(user1);
		list.add(user2);
		return list;
		
	}
	
	@PostMapping(value="testUserAccountRegistNoRepeatSubmit",
			name="测试注册用户账号接口防重复提交功能")
	@ApiOperation(value="测试注册用户账号接口防重复提交功能", notes="防重复提交码由客户端生成，防止同一用户重复注册!")
	@RequiredNoRepeatSubmit(mode = DenyRepeatSubmitType.GENERATE_TOKEN)
	public void testUserAccountRegistNoRepeatSubmit(@RequestBody UserInfoDto userInfo){
		log.info("保存用户信息成功[{}]!",userInfo.toString());
	}
	
	@PostMapping(value="testUserAccountRegistNoRepeatSubmitBySign",
			name="测试注册用户账号接口防重复提交功能数字签名模式")
	@ApiOperation(value="测试注册用户账号接口防重复提交功能数字签名模式", notes="使用数字签名，防止同一用户重复注册!")
	@RequiredNoRepeatSubmit(mode = DenyRepeatSubmitType.USE_SIGN)
	@RequiredSign
	public void testUserAccountRegistNoRepeatSubmitBySign(@RequestBody UserInfoDto userInfo){
		log.info("保存用户信息成功[{}]!",userInfo.toString());
	}
}
