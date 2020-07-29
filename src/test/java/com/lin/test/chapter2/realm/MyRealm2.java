package com.lin.test.chapter2.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.slf4j.LoggerFactory;

public class MyRealm2 implements Realm {

	org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "myRealm2";
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		// 仅支持UsernamePasswordToken类型的Token  
		return token instanceof UsernamePasswordToken;
	}

	@Override
	public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String) token.getPrincipal(); //得到用户名
		String password = new String((char[])token.getCredentials());//得到密码  
		if (!"wang".equals(username)) {
			throw new UnknownAccountException();//如果用户名错误  
		}
		if (!"123".equals(password)) {
			throw new IncorrectCredentialsException();//如果密码错误
		}
		//如果身份认证验证成功，返回一个AuthenticationInfo实现；
		log.info("zhang 123 login success");
		return new SimpleAuthenticationInfo(username, password, getName());
	}

}
