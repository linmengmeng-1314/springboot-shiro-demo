package com.lin.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class MyToken implements AuthenticationToken{

	private String token;
	
	public MyToken(String token) {
		this.token = token;
	}

	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return token;
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return token;
	}

}
