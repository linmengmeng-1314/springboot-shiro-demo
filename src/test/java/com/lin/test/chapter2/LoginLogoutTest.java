package com.lin.test.chapter2;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lin.test.Tutorial;

public class LoginLogoutTest {
	
	private static final transient Logger log = LoggerFactory.getLogger(Tutorial.class);

	@Test
	public void testCustomRealm(){
		log.info("testCustomRealm");
//		//1、获取SecurityManager工厂，此处使用Ini配置文件初始化SecurityManager
//		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-realm.ini");
//		
//		//2、得到SecurityManager实例 并绑定给SecurityUtils
//		SecurityManager securityManager = factory.getInstance();
//		SecurityUtils.setSecurityManager(securityManager);
//		
//		//3、得到Subject及创建用户名/密码身份验证Token（即用户身份/凭证）
//		Subject subject = SecurityUtils.getSubject();
		String iniFileName = "shiro-realm.ini";
		Subject subject = getSubjectByIniFile(iniFileName);
		UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken("zhang", "123");
		
		 try {
			//4、登录，即身份验证
			subject.login(usernamePasswordToken);
		} catch (AuthenticationException e) {
			//5、身份验证失败
			e.printStackTrace();
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//6.退出登录
		subject.logout();
	}
	
	
	@Test
	public void testCustomMultiRealm(){
		String iniFileName = "shiro-multi-realm.ini";
		Subject subject = getSubjectByIniFile(iniFileName);
		
		UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken("wang", "123");
		
		 try {
			//4、登录，即身份验证
			subject.login(usernamePasswordToken);
		} catch (AuthenticationException e) {
			//5、身份验证失败
			e.printStackTrace();
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//6.退出登录
		subject.logout();
	}
	
	/**
	 * 根据ini文件名称获取subject
	 * @auther linmengmeng
	 * @Date 2020-07-29 下午3:30:34
	 * @param iniFileName ini文件名称，带后缀名
	 * @return
	 */
	private Subject getSubjectByIniFile(String iniFileName){
		log.info("getSubjectByIniFile");
		//1、获取SecurityManager工厂，此处使用Ini配置文件初始化SecurityManager
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:" + iniFileName);
		
		//2、得到SecurityManager实例 并绑定给SecurityUtils
		SecurityManager securityManager = factory.getInstance();
		SecurityUtils.setSecurityManager(securityManager);
		
		//3、得到Subject及创建用户名/密码身份验证Token（即用户身份/凭证）
		return SecurityUtils.getSubject();
	}

}
