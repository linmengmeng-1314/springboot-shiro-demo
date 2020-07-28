package com.lin.test;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Tutorial {

	private static final transient Logger log = LoggerFactory.getLogger(Tutorial.class);

    public static void main(String[] args) {
        log.info("My First Apache Shiro Application");
        
        //仅仅使用三行代码就把Shiro加进了我们的程序
        //使用 Shiro 的 IniSecurityManagerFactory 加载了我们的shiro.ini 文件  (工厂模式)
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        //分析 INI 文件并根据配置文件返回一个 SecurityManager 实例。
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        
        Subject currentUser = SecurityUtils.getSubject();
        
        Session session = currentUser.getSession();
        session.setAttribute( "someKey", "aValue" );
        
        String value = (String)session.getAttribute("someKey");
        
        if (value.equals("aValue")) {
        	log.info("Retrieved the correct value! [" + value + "]");
		}
        
        // 检验当前登录用户角色和权限
        System.out.println("isAuthenticated:" + currentUser.isAuthenticated());
        if (!currentUser.isAuthenticated()) {
			UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
			token.setRememberMe(true);
			try {
				currentUser.login(token);
			} catch (UnknownAccountException uae) {//当前用户lonestarr1不存在
                log.info("There is no user with username of " + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {//用户lonestarr密码错误
                log.info("Password for account " + token.getPrincipal() + " was incorrect!");
            } catch (LockedAccountException lae) {
                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
                        "Please contact your administrator to unlock it.");
            }
            // ... 捕获更多异常
            catch (AuthenticationException ae) {
                //无定义?错误?
            	log.info("无定义?错误?");
            }
			
			 //打印主要识别信息 (本例是 username):
	        log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");
	        
	        //测试角色:
	        if (currentUser.hasRole("schwartz")) {
	            log.info("May the Schwartz be with you!");
	        } else {
	            log.info("Hello, mere mortal.");
	        }
	        
	        //测试一个权限 (非（instance-level）实例级别)
	        if (currentUser.isPermitted("lightsaber:weild")) {
	            log.info("You may use a lightsaber ring.  Use it wisely.");
	        } else {
	            log.info("Sorry, lightsaber rings are for schwartz masters only.");
	        }
	        
	        //一个(非常强大)的实例级别的权限:
	        if (currentUser.isPermitted("winnebago:drive:eagle5")) {
	            log.info("You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  " +
	                    "Here are the keys - have fun!");
	        } else {
	            log.info("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
	        }

	        //完成 - 退出t!
	        currentUser.logout();
		}
        
        System.exit(0);
    }
}
