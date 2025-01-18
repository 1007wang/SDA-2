package com.sda_2.Controller;

import com.sda_2.DTO.StatisticsRequest;
import com.sda_2.Service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping("/linear-regression")
    public Map<String, Object> getLinearRegression(@RequestBody StatisticsRequest request) {
        return statisticsService.calculateLinearRegression(request.getData());
    }

    @PostMapping("/polynomial-regression")
    public Map<String, Object> getPolynomialRegression(
            @RequestBody StatisticsRequest request) {
        return statisticsService.calculatePolynomialRegression(request.getData(), request.getDegree());
    }

    @PostMapping("/normal-distribution")
    public Map<String, Object> getNormalDistribution(@RequestBody StatisticsRequest request) {
        return statisticsService.calculateNormalDistribution(request.getData());
    }

    @PostMapping("/binomial-distribution")
    public Map<String, Object> getBinomialDistribution(@RequestBody StatisticsRequest request) {
        return statisticsService.calculateBinomialDistribution(request.getData());
    }

    @PostMapping("/box-plot")
    public Map<String, Object> getBoxPlot(@RequestBody StatisticsRequest request) {
        return statisticsService.calculateBoxPlot(request.getData());
    }
} 