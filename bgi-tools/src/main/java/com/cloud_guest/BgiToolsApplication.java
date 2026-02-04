package com.cloud_guest;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BgiToolsApplication {

	public static void main(String[] args) {
		//SpringApplication app = new SpringApplication(BgiToolsApplication.class);
		//// 将 Banner 输出到日志
		//app.setBannerMode(Banner.Mode.LOG);
		//app.run(args);
		SpringApplication.run(BgiToolsApplication.class, args);
	}

}
