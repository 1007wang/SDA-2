<template>
  <div class="data-analysis">
    <!-- 查询表单 -->
    <el-card class="query-form">
      <template #header>
        <div class="card-header">
          <span>数据查询</span>
        </div>
      </template>
      <el-form :model="queryForm" label-width="120px">
        <el-form-item label="地区">
          <el-select v-model="queryForm.geography" placeholder="请选择地区">
            <el-option label="Canada" value="Canada" />
            <el-option label="United States" value="United States" />
            <!-- 添加更多地区选项 -->
          </el-select>
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select v-model="queryForm.analysisType" placeholder="请选择分析类型">
            <el-option label="线性回归" value="linear" />
            <el-option label="多项式回归" value="polynomial" />
            <el-option label="正态分布" value="normal" />
            <el-option label="二项分布" value="binomial" />
            <el-option label="箱线图" value="boxplot" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="queryForm.analysisType === 'polynomial'" label="多项式阶数">
          <el-input-number v-model="queryForm.degree" :min="1" :max="5" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAnalyze">分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分析结果 -->
    <el-card v-if="analysisResult" class="analysis-result">
      <template #header>
        <div class="card-header">
          <span>分析结果</span>
        </div>
      </template>
      
      <!-- 图表展示 -->
      <div class="chart-container" ref="chartRef"></div>

      <!-- 数据结果 -->
      <div class="result-details">
        <template v-if="queryForm.analysisType === 'linear'">
          <h3>线性回归结果</h3>
          <p>斜率: {{ analysisResult.slope?.toFixed(4) }}</p>
          <p>截距: {{ analysisResult.intercept?.toFixed(4) }}</p>
          <p>R方值: {{ analysisResult.r_squared?.toFixed(4) }}</p>
          <p>相关系数: {{ analysisResult.correlation?.toFixed(4) }}</p>
        </template>

        <template v-if="queryForm.analysisType === 'normal'">
          <h3>正态分布拟合结果</h3>
          <p>均值: {{ analysisResult.mean?.toFixed(4) }}</p>
          <p>标准差: {{ analysisResult.standard_deviation?.toFixed(4) }}</p>
          <p>P值: {{ analysisResult.p_value?.toFixed(4) }}</p>
          <p>是否符合正态分布: {{ analysisResult.is_normal ? '是' : '否' }}</p>
        </template>

        <template v-if="queryForm.analysisType === 'binomial'">
          <h3>二项分布拟合结果</h3>
          <p>试验次数: {{ analysisResult.trials }}</p>
          <p>成功概率: {{ analysisResult.probability?.toFixed(4) }}</p>
          <p>期望值: {{ analysisResult.expected_mean?.toFixed(4) }}</p>
          <p>方差: {{ analysisResult.expected_variance?.toFixed(4) }}</p>
        </template>

        <template v-if="queryForm.analysisType === 'boxplot'">
          <h3>箱线图统计结果</h3>
          <p>最小值: {{ analysisResult.min?.toFixed(4) }}</p>
          <p>Q1: {{ analysisResult.q1?.toFixed(4) }}</p>
          <p>中位数: {{ analysisResult.median?.toFixed(4) }}</p>
          <p>Q3: {{ analysisResult.q3?.toFixed(4) }}</p>
          <p>最大值: {{ analysisResult.max?.toFixed(4) }}</p>
          <p>IQR: {{ analysisResult.iqr?.toFixed(4) }}</p>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { statisticsApi } from '../api';
import * as echarts from 'echarts';

const queryForm = ref({
  geography: '',
  analysisType: '',
  degree: 2
});

const analysisResult = ref(null);
const chartRef = ref(null);
let chart = null;

// 初始化图表
onMounted(() => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value);
  }
});

// 处理分析请求
const handleAnalyze = async () => {
  try {
    const { geography, analysisType, degree } = queryForm.value;
    let response;

    switch (analysisType) {
      case 'linear':
        response = await statisticsApi.getLinearRegression(geography);
        break;
      case 'polynomial':
        response = await statisticsApi.getPolynomialRegression(geography, degree);
        break;
      case 'normal':
        response = await statisticsApi.getNormalDistribution(geography);
        break;
      case 'binomial':
        response = await statisticsApi.getBinomialDistribution(geography);
        break;
      case 'boxplot':
        response = await statisticsApi.getBoxPlot(geography);
        break;
    }

    analysisResult.value = response.data;
    updateChart();
  } catch (error) {
    console.error('Analysis failed:', error);
    ElMessage.error('分析失败，请重试');
  }
};

// 更新图表
const updateChart = () => {
  if (!chart || !analysisResult.value) return;

  const option = {
    title: {
      text: `${queryForm.value.geography} - ${getAnalysisTypeText()}`
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['Q1', 'Q2', 'Q3', 'Q4'] // 示例数据，需要根据实际数据调整
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: getChartType(),
        data: getChartData()
      }
    ]
  };

  chart.setOption(option);
};

// 获取分析类型文本
const getAnalysisTypeText = () => {
  const typeMap = {
    linear: '线性回归分析',
    polynomial: '多项式回归分析',
    normal: '正态分布拟合',
    binomial: '二项分布拟合',
    boxplot: '箱线图分析'
  };
  return typeMap[queryForm.value.analysisType] || '';
};

// 获取图表类型
const getChartType = () => {
  const typeMap = {
    linear: 'line',
    polynomial: 'line',
    normal: 'bar',
    binomial: 'bar',
    boxplot: 'boxplot'
  };
  return typeMap[queryForm.value.analysisType] || 'line';
};

// 获取图表数据
const getChartData = () => {
  // 根据不同的分析类型返回相应的数据格式
  const result = analysisResult.value;
  switch (queryForm.value.analysisType) {
    case 'boxplot':
      return [[result.min, result.q1, result.median, result.q3, result.max]];
    // 其他类型的数据处理...
    default:
      return [];
  }
};

// 监听窗口大小变化
window.addEventListener('resize', () => {
  chart?.resize();
});
</script>

<style scoped>
.data-analysis {
  padding: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
  margin-bottom: 20px;
}

.result-details {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.result-details h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.result-details p {
  margin: 8px 0;
  color: #606266;
}
</style> 