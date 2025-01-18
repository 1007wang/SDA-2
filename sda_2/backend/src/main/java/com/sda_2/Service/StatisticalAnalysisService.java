package com.sda_2.Service;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticalAnalysisService {

    // 线性回归分析
    public Map<String, Double> performLinearRegression(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("数据集大小不匹配或为空");
        }

        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < x.size(); i++) {
            regression.addData(x.get(i), y.get(i));
        }

        Map<String, Double> results = new HashMap<>();
        results.put("slope", regression.getSlope());
        results.put("intercept", regression.getIntercept());
        results.put("r_squared", regression.getRSquare());
        results.put("correlation", new PearsonsCorrelation().correlation(
            x.stream().mapToDouble(Double::doubleValue).toArray(),
            y.stream().mapToDouble(Double::doubleValue).toArray()
        ));

        return results;
    }

    // 非线性回归（多项式拟合）
    public Map<String, Double> performPolynomialRegression(List<Double> x, List<Double> y, int degree) {
        WeightedObservedPoints points = new WeightedObservedPoints();
        for (int i = 0; i < x.size(); i++) {
            points.add(x.get(i), y.get(i));
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
        double[] coefficients = fitter.fit(points.toList());

        Map<String, Double> results = new HashMap<>();
        for (int i = 0; i < coefficients.length; i++) {
            results.put("coefficient_" + i, coefficients[i]);
        }

        return results;
    }

    // 正态分布拟合
    public Map<String, Object> fitNormalDistribution(List<Double> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        NormalDistribution normalDist = new NormalDistribution(mean, std);

        // 进行Kolmogorov-Smirnov检验
        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
        double[] dataArray = data.stream().mapToDouble(Double::doubleValue).toArray();
        double pValue = ksTest.kolmogorovSmirnovTest(normalDist, dataArray);

        Map<String, Object> results = new HashMap<>();
        results.put("mean", mean);
        results.put("standard_deviation", std);
        results.put("p_value", pValue);
        results.put("is_normal", pValue > 0.05);

        return results;
    }

    // 二项分布拟合
    public Map<String, Object> fitBinomialDistribution(List<Integer> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        int n = data.stream().mapToInt(Integer::intValue).max().orElse(0);
        double p = stats.getMean() / n;

        BinomialDistribution binomialDist = new BinomialDistribution(n, p);

        Map<String, Object> results = new HashMap<>();
        results.put("trials", n);
        results.put("probability", p);
        results.put("expected_mean", binomialDist.getNumericalMean());
        results.put("expected_variance", binomialDist.getNumericalVariance());

        return results;
    }

    // 计算四分位数和箱线图数据
    public Map<String, Double> calculateBoxPlotStats(List<Double> data) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        data.forEach(stats::addValue);

        Map<String, Double> results = new HashMap<>();
        results.put("min", stats.getMin());
        results.put("q1", stats.getPercentile(25));
        results.put("median", stats.getPercentile(50));
        results.put("q3", stats.getPercentile(75));
        results.put("max", stats.getMax());
        results.put("iqr", results.get("q3") - results.get("q1"));

        return results;
    }
} 