package com.sda_2.Service;

import com.sda_2.DTO.PopulationData;
import com.sda_2.DTO.QuarterPopulationData;
import com.sda_2.Mappers.PopulationMapper;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PopulationService {

    private final PopulationMapper populationMapper;

    public PopulationService(PopulationMapper populationMapper) {
        this.populationMapper = populationMapper;
    }

    // 查询所有地区所有季度人口数据
    public List<PopulationData> getAllData() {
        return populationMapper.findAll();
    }

    // 根据地区名称查询该地区各季度人口数据
    public PopulationData getPopulationByGeography(String geography) {
        PopulationData data = populationMapper.findByGeography(geography);
        if (data == null) {
            throw new IllegalArgumentException("Geography not found: " + geography);
        }
        return data;
    }

    // 根据季度查询所有地区的人口数据
    public List<QuarterPopulationData> getPopulationByQuarter(String quarter) {
        validateQuarter(quarter);
        return populationMapper.findByQuarter(quarter);
    }

    // 查询某个季度的总人口
    public Long getTotalPopulationByQuarter(String quarter) {
        validateQuarter(quarter);
        Long total = populationMapper.findTotalByQuarter(quarter);
        return total != null ? total : 0L;
    }

    // 校验季度字段名是否合法
    private void validateQuarter(String quarter) {
        if (quarter == null || quarter.trim().isEmpty()) {
            throw new IllegalArgumentException("Quarter cannot be null or empty");
        }
        
        // 检查格式是否正确
        if (!quarter.matches("Q[1-4] \\d{4}")) {
            throw new IllegalArgumentException("Invalid quarter format. Expected format: 'Q1 2024'");
        }

        // 解析年份和季度
        String[] parts = quarter.split(" ");
        int q = Integer.parseInt(parts[0].substring(1));
        int year = Integer.parseInt(parts[1]);

        // 检查年份和季度范围
        if (year < 1991 || year > 2024) {
            throw new IllegalArgumentException("Year must be between 1991 and 2024");
        }
        
        // 特殊处理1991年和2024年的季度限制
        if ((year == 1991 && q < 1) || (year == 2024 && q > 4)) {
            throw new IllegalArgumentException("Invalid quarter for the specified year");
        }
    }

    // 计算人口均值、众数和中位数
    public Map<String, Double> calculatePopulationStatistics(List<Long> populationData) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long population : populationData) {
            stats.addValue(population);
        }

        Map<String, Double> statistics = new HashMap<>();
        statistics.put("mean", stats.getMean());
        statistics.put("mode", calculateMode(populationData)); // 计算众数
        statistics.put("median", stats.getPercentile(50)); // 中位数

        return statistics;
    }

    // 计算标准差，Q1，Q3，IQR和百分位数
    public Map<String, Double> calculatePopulationDistribution(List<Long> populationData) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long population : populationData) {
            stats.addValue(population);
        }

        Map<String, Double> distribution = new HashMap<>();
        distribution.put("stddev", stats.getStandardDeviation()); // 标准差
        distribution.put("q1", stats.getPercentile(25)); // Q1
        distribution.put("q3", stats.getPercentile(75)); // Q3
        distribution.put("iqr", stats.getPercentile(75) - stats.getPercentile(25)); // IQR
        distribution.put("percentile_90", stats.getPercentile(90)); // 90百分位

        return distribution;
    }

    // 计算数组的范围，即最大值与最小值之差
    private double calculateRange(double[] array) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double value : array) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        return max - min;
    }

    // 计算线性回归的相关系数，拟合直线方程
    public Map<String, Double> calculateLinearRegression(List<Long> populationData) {
        // 假设x为时间或季度的顺序，y为人口数
        if (populationData.size() < 2) {
            throw new IllegalArgumentException("Insufficient data for regression analysis.");
        }

        double[] x = new double[populationData.size()];
        double[] y = new double[populationData.size()];

        // 使用季度号作为x值
        for (int i = 0; i < populationData.size(); i++) {
            x[i] = i + 1; // 使用顺序编号 1, 2, 3, ... 作为时间序列
            y[i] = populationData.get(i);
        }

        // 检查x和y是否有足够的变化
        double xRange = calculateRange(x);
        double yRange = calculateRange(y);

        if (xRange == 0 || yRange == 0) {
            throw new IllegalArgumentException("Data has no variation. Cannot perform regression.");
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();

        // 由于只有一个自变量（x），构造矩阵的方式是：[x1, x2, ..., xn]。我们需要给回归模型提供一个2D数组。
        double[][] xMatrix = new double[x.length][1]; // 每个样本对应一个自变量（x）
        for (int i = 0; i < x.length; i++) {
            xMatrix[i][0] = x[i];
        }

        regression.newSampleData(y, xMatrix);  // xMatrix 是一个二维数组，适用于单变量回归

        try {
            double[] regressionParams = regression.estimateRegressionParameters();
            double rSquared = regression.calculateRSquared();

            Map<String, Double> regressionResult = new HashMap<>();
            regressionResult.put("slope", regressionParams[1]); // 斜率
            regressionResult.put("intercept", regressionParams[0]); // 截距
            regressionResult.put("rSquared", rSquared); // 相关系数

            return regressionResult;
        } catch (Exception e) {
            throw new RuntimeException("Error performing regression: " + e.getMessage());
        }
    }


    // 求众数
    private double calculateMode(List<Long> populationData) {
        Map<Long, Integer> frequencyMap = new HashMap<>();
        for (Long population : populationData) {
            frequencyMap.put(population, frequencyMap.getOrDefault(population, 0) + 1);
        }

        long mode = populationData.get(0);
        int maxCount = 0;
        for (Map.Entry<Long, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }
        return mode;
    }

    // 根据地区或季度查询并获取数据
    public List<Long> getPopulationData(String geography, String quarter) {
        List<Long> populationData = new ArrayList<>();

        if (geography != null && !geography.isEmpty()) {
            // 获取该地区的所有季度人口数据
            PopulationData data = populationMapper.findByGeography(geography);
            populationData = extractPopulationData(data);
        } else if (quarter != null && !quarter.isEmpty()) {
            // 获取该季度的所有地区人口数据
            List<QuarterPopulationData> data = populationMapper.findByQuarter(quarter);
            for (QuarterPopulationData quarterData : data) {
                populationData.add(quarterData.getPopulation());
            }
        }

        return populationData;
    }

    private List<Long> extractPopulationData(PopulationData data) {
        List<Long> populationData = new ArrayList<>();
        if (data != null && data.getQuarterData() != null) {
            // 按照时间顺序提取所有季度的数据
            for (int year = 1991; year <= 2024; year++) {
                for (int q = 1; q <= 4; q++) {
                    if (year == 1991 && q < 4) continue;
                    if (year == 2024 && q > 2) break;
                    String quarter = String.format("Q%d %d", q, year);
                    Integer value = data.getQuarterValue(quarter);
                    populationData.add(value != null ? value.longValue() : 0L);
                }
            }
        }
        return populationData;
    }

    // 获取所有地区列表
    public List<String> getAllGeographies() {
        List<PopulationData> allData = populationMapper.findAll();
        return allData.stream()
                .map(PopulationData::getGeography)
                .distinct()
                .collect(Collectors.toList());
    }
}
