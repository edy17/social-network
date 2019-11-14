package org.diehl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods=false)
public class CommandlinerunnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommandlinerunnerApplication.class, args);
	}
	
}
