package com.sda_2.Controller;

import com.sda_2.Service.StatisticalAnalysisService;
import com.sda_2.Service.PopulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class StatisticalAnalysisController {

    private final StatisticalAnalysisService statisticalAnalysisService;
    private final PopulationService populationService;

    public StatisticalAnalysisController(
            StatisticalAnalysisService statisticalAnalysisService,
            PopulationService populationService) {
        this.statisticalAnalysisService = statisticalAnalysisService;
        this.populationService = populationService;
    }

    @GetMapping("/linear-regression/{geography}")
    public ResponseEntity<Map<String, Double>> getLinearRegression(
            @PathVariable String geography) {
        List<Long> populationData = populationService.getPopulationData(geography, null);
        List<Double> timePoints = populationData.stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());
        List<Double> populations = populationData.stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                statisticalAnalysisService.performLinearRegression(timePoints, populations));
    }

    @GetMapping("/polynomial-regression/{geography}")
    public ResponseEntity<?> getPolynomialRegression(
            @PathVariable String geography,
            @RequestParam(defaultValue = "2") int degree) {
        if (degree < 1 || degree > 5) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "degree参数必须在1到5之间"));
        }

        try {
            List<Long> populationData = populationService.getPopulationData(geography, null);
            if (populationData == null || populationData.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "未找到指定地区的数据"));
            }

            List<Double> timePoints = populationData.stream()
                    .map(Double::valueOf)
                    .collect(Collectors.toList());
            List<Double> populations = populationData.stream()
                    .map(Double::valueOf)
                    .collect(Collectors.toList());

            Map<String, Double> result = statisticalAnalysisService
                .performPolynomialRegression(timePoints, populations, degree);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "计算多项式回归时发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/normal-distribution/{geography}")
    public ResponseEntity<Map<String, Object>> getNormalDistributionFit(
            @PathVariable String geography) {
        List<Long> populationData = populationService.getPopulationData(geography, null);
        List<Double> populations = populationData.stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                statisticalAnalysisService.fitNormalDistribution(populations));
    }

    @GetMapping("/binomial-distribution/{geography}")
    public ResponseEntity<Map<String, Object>> getBinomialDistributionFit(
            @PathVariable String geography) {
        List<Long> populationData = populationService.getPopulationData(geography, null);
        List<Integer> populations = populationData.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                statisticalAnalysisService.fitBinomialDistribution(populations));
    }

    @GetMapping("/box-plot/{geography}")
    public ResponseEntity<Map<String, Double>> getBoxPlotStats(
            @PathVariable String geography) {
        List<Long> populationData = populationService.getPopulationData(geography, null);
        List<Double> populations = populationData.stream()
                .map(Double::valueOf)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                statisticalAnalysisService.calculateBoxPlotStats(populations));
    }
} 