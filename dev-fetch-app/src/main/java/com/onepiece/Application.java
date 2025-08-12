package com.onepiece;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Configurable
public class Application {

    public static void main(String[] args){
        System.out.println("PATH: " + System.getenv("PATH"));
        System.out.println("NVM_DIR: " + System.getenv("NVM_DIR"));
        SpringApplication.run(Application.class);
    }

}
