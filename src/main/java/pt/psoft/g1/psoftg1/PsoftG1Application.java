package pt.psoft.g1.psoftg1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PsoftG1Application {

	public static void main(String[] args) {
		SpringApplication.run(PsoftG1Application.class, args);
	}

}
