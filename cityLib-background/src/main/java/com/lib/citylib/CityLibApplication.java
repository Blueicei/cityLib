package com.lib.citylib;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lib.citylib.**"})
@MapperScan(basePackages = {"com.lib,citylib.**"})
public class CityLibApplication {
    public static void main(String[] args) {
        SpringApplication.run(CityLibApplication.class, args);
    }

}
