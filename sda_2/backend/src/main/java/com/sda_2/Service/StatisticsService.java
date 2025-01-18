package com.sda_2.Service;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsService {

    public Map<String, Object> calculateLinearRegression(List<Double> data) {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.size(); i++) {
            regression.addData(i, data.get(i));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("slope", regression.getSlope());
        result.put("intercept", regression.getIntercept());
        result.put("r_squared", regression.getRSquare());
        result.put("correlation", regression.getR());
        return result;
    }

    public Map<String, Object> calculatePolynomialRegression(List<Double> data, Integer degree) {
        if (degree == null || degree < 1 || degree > 5) {
            degree = 2; // 默认使用2次多项式
        }

        int n = data.size();
        if (n < degree + 1) {
            throw new IllegalArgumentException("数据点数量不足以进行" + degree + "次多项式拟合");
        }

        double[][] x = new double[n][degree + 1];
        double[] y = new double[n];

        // 准备数据
        for (int i = 0; i < n; i++) {
            Double value = data.get(i);
            if (value == null) {
                throw new IllegalArgumentException("数据包含空值");
            }
            for (int j = 0; j <= degree; j++) {
                x[i][j] = Math.pow(i, j);
            }
            y[i] = value;
        }

        try {
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            Map<String, Object> result = new HashMap<>();
            result.put("coefficients", regression.estimateRegressionParameters());
            result.put("r_squared", regression.calculateRSquared());
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("多项式回归计算失败: " + e.getMessage());
        }
    }

    public Map<String, Object> calculateNormalDistribution(List<Double> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        NormalDistribution normalDist = new NormalDistribution(mean, std);

        // 计算 Shapiro-Wilk 检验的 P 值
        double[] sortedData = data.stream().mapToDouble(d -> d).sorted().toArray();
        double w = calculateShapiroWilkW(sortedData);
        double pValue = 1 - w; // 简化的 P 值计算

        Map<String, Object> result = new HashMap<>();
        result.put("mean", mean);
        result.put("standard_deviation", std);
        result.put("p_value", pValue);
        result.put("is_normal", pValue > 0.05);
        return result;
    }

    public Map<String, Object> calculateBinomialDistribution(List<Double> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        double mean = stats.getMean();
        double max = stats.getMax();
        int trials = (int) max;
        double probability = mean / trials;

        Map<String, Object> result = new HashMap<>();
        result.put("trials", trials);
        result.put("probability", probability);
        result.put("expected_mean", trials * probability);
        result.put("expected_variance", trials * probability * (1 - probability));
        return result;
    }

    public Map<String, Object> calculateBoxPlot(List<Double> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        Map<String, Object> result = new HashMap<>();
        result.put("min", stats.getMin());
        result.put("q1", stats.getPercentile(25));
        result.put("median", stats.getPercentile(50));
        result.put("q3", stats.getPercentile(75));
        result.put("max", stats.getMax());
        result.put("iqr", stats.getPercentile(75) - stats.getPercentile(25));
        return result;
    }

    // Shapiro-Wilk W 统计量计算（简化版本）
    private double calculateShapiroWilkW(double[] sortedData) {
        int n = sortedData.length;
        double mean = StatUtils.mean(sortedData);
        double denominator = 0;
        for (double value : sortedData) {
            denominator += Math.pow(value - mean, 2);
        }

        double numerator = 0;
        for (int i = 0; i < n / 2; i++) {
            double weight = getShapiroWilkWeight(i + 1, n);
            numerator += weight * (sortedData[n - 1 - i] - sortedData[i]);
        }
        numerator = Math.pow(numerator, 2);

        return numerator / denominator;
    }

    // 简化的 Shapiro-Wilk 权重计算
    private double getShapiroWilkWeight(int i, int n) {
        return 1.0 / Math.sqrt(n);
    }
} 