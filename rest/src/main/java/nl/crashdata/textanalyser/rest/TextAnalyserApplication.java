package nl.crashdata.textanalyser.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses=TextAnalyserApplication.class)
public class TextAnalyserApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(TextAnalyserApplication.class, args);
	}
}
