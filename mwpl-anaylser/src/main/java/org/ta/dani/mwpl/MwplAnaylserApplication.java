package org.ta.dani.mwpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MwplAnaylserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MwplAnaylserApplication.class, args);
	}

}
