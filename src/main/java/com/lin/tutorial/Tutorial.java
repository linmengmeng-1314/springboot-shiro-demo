package com.lin.tutorial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tutorial {

	private static final transient Logger log = LoggerFactory.getLogger(Tutorial.class);

    public static void main(String[] args) {
    	System.out.println("11111111");
        log.info("My First Apache Shiro Application");
        System.exit(0);
    }
}
