package ai.carmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class CarMatchAiApplication {

	public static void main(String[] args) {
		// Set default timezone to UTC to avoid timezone issues
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(CarMatchAiApplication.class, args);
	}

}
