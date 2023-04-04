package com.example.demo.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GeocodingService {

	private final RestTemplate restTemplate;
	
	@Autowired
	public GeocodingService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public List<String> readAddressesFromFile(String filename) throws IOException {
		return Files.readAllLines(Paths.get(filename));
	}

	private String nominatimUrl = "https://nominatim.openstreetmap.org/search?q=";
	private String nominatimParams = "&format=json&limit=1";

//	private String nominatimUrl2 = "https://nominatim.openstreetmap.org/search";
	
	public Optional<LatLng> geocodeAddress(String address) throws URISyntaxException {
		
		URI uri = new URI(nominatimUrl + UriUtils.encode(address, StandardCharsets.UTF_8) + nominatimParams);
		
		try {
			
			ResponseEntity<JsonNode[]> response = restTemplate.getForEntity(uri,JsonNode[].class);
			JsonNode location = response.getBody()[0];
			return Optional.of(new LatLng(location.get("lat").asDouble(), location.get("lon").asDouble()));
		} catch (Exception e) {
			// Handle error, e.g., address not found or API error
			return Optional.empty();
		}
	}

	public static class LatLng {
		public double lat;
		public double lng;

		public LatLng(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}

	public void writeToExcelFile(List<String> addresses, String outputFilepath) throws FileNotFoundException, IOException, URISyntaxException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Geocoded Addresses");

		// Add header row
		XSSFRow header = sheet.createRow(0);
		header.createCell(0).setCellValue("Address");
		header.createCell(1).setCellValue("Latitude");
		header.createCell(2).setCellValue("Longitude");

		// Add address rows
		for (int i = 0; i < addresses.size(); i++) {
			String address = addresses.get(i);
			Optional<LatLng> latLng = geocodeAddress(address);
			if (latLng.isPresent()) {
				XSSFRow row = sheet.createRow(i + 1);
				row.createCell(0).setCellValue(address);
				row.createCell(1).setCellValue(latLng.get().lat);
				row.createCell(2).setCellValue(latLng.get().lng);
			}
		}
		try (FileOutputStream outputStream = new FileOutputStream(outputFilepath)) {
            workbook.write(outputStream);
        }
		finally {
			workbook.close();
		}

	}
}
