package com.example.demo.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService(restTemplate);
    }

    @Test
    void geocodeAddress_validAddress_returnsLatLng() throws Exception {
        // Prepare test data
        String address = "1600 Amphitheatre Parkway, Mountain View, CA";
        String jsonResponse = "[{\"place_id\":\"123\",\"lat\":\"37.4219999\",\"lon\":\"-122.0840575\"}]";

        // Prepare mock response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode[] responseNodes = objectMapper.readValue(jsonResponse, JsonNode[].class);
        ResponseEntity<JsonNode[]> responseEntity = ResponseEntity.ok(responseNodes);
        Mockito.when(restTemplate.getForEntity(any(URI.class), eq(JsonNode[].class)))
                .thenReturn(responseEntity);

        // Call the method
        Optional<GeocodingService.LatLng> result = geocodingService.geocodeAddress(address);

        // Assert the result
        assertThat(result).isPresent();
        assertThat(result.get().lat).isEqualTo(37.4219999);
        assertThat(result.get().lng).isEqualTo(-122.0840575);

        // Verify the interactions
        Mockito.verify(restTemplate).getForEntity(any(URI.class), eq(JsonNode[].class));
    }

    @Test
    void geocodeAddress_invalidAddress_returnsEmptyOptional() throws URISyntaxException {
        // Prepare test data
        String address = "nonexistent address";

        // Prepare mock response
        Mockito.when(restTemplate.getForEntity(any(String.class), eq(JsonNode[].class)))
                .thenReturn(ResponseEntity.ok(new JsonNode[0]));

        // Call the method
        Optional<GeocodingService.LatLng> result = geocodingService.geocodeAddress(address);

        // Assert the result
        assertThat(result).isEmpty();

        // Verify the interactions
        Mockito.verify(restTemplate).getForEntity(any(URI.class), eq(JsonNode[].class));
    }
}
