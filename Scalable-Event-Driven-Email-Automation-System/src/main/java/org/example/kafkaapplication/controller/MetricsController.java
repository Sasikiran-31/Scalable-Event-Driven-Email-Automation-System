package org.example.kafkaapplication.Controller;

import org.example.kafkaapplication.Service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoint to view the health checks - Lists the live count of the status of the events
 */
@RestController
public class MetricsController {

    public final MetricsService metricsService;

    @Autowired
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/api/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        return new  ResponseEntity<>(metricsService.getMetrics(), HttpStatus.OK);
    }





}
