package com.sda_2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sda_2.Mappers")
public class Sda2Application {

	public static void main(String[] args) {
		SpringApplication.run(Sda2Application.class, args);
	}

}
