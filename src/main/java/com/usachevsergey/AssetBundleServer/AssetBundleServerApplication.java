package com.usachevsergey.AssetBundleServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AssetBundleServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssetBundleServerApplication.class, args);
	}

}
