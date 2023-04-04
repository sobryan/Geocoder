package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.service.GeocodingService;

@SpringBootApplication
public class GeocoderApplication implements CommandLineRunner {

	@Autowired
	GeocodingService geocodingService;
	
	public static void main(String[] args) {
		SpringApplication.run(GeocoderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		String inputFilePath = "C:\\nonsync\\files\\addressfile.txt";
		List<String> addresses = geocodingService.readAddressesFromFile(inputFilePath);
		String outputFilePath = "C:\\nonsync\\files\\outputFile.xlsx";
		geocodingService.writeToExcelFile(addresses, outputFilePath);
		System.out.println("Completed");
		System.exit(1);
		
		
	}
	

}
