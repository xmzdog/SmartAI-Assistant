package com.onepiece;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {


    public static void main(String[] args) {

//        System.out.println("==== 当前 spring-core 来源 ====");
//        System.out.println(org.springframework.core.SpringProperties.class
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation());
//
//        System.out.println("==== 当前 spring-jdbc 来源 ====");
//        System.out.println(org.springframework.jdbc.core.JdbcTemplate.class
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation());
//
//        System.out.println("StatementCreatorUtils 来源: " +
//                org.springframework.jdbc.core.StatementCreatorUtils.class
//                        .getProtectionDomain()
//                        .getCodeSource()
//                        .getLocation());

        SpringApplication.run(Application.class);
    }

}
