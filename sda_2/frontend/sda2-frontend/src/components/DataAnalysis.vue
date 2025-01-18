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
        <!-- 查询类型选择 -->
        <el-form-item label="查询类型">
          <el-radio-group v-model="queryForm.queryType">
            <el-radio label="geography">按地区查询</el-radio>
            <el-radio label="quarter">按季度查询</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 地区选择 -->
        <el-form-item v-if="queryForm.queryType === 'geography'" label="地区">
          <el-select v-model="queryForm.geography" placeholder="请选择地区">
            <el-option
              v-for="item in geographyOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

        <!-- 季度选择 -->
        <el-form-item v-if="queryForm.queryType === 'quarter'" label="季度">
          <el-select v-model="queryForm.quarter" placeholder="请选择季度">
            <el-option
              v-for="option in quarterOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <!-- 统计分析类型 -->
        <el-form-item label="统计分析">
          <el-select v-model="queryForm.analysisType" placeholder="请选择统计分析类型">
            <el-option label="线性回归" value="linear" />
            <el-option label="多项式回归" value="polynomial" />
            <el-option label="正态分布" value="normal" />
            <el-option label="二项分布" value="binomial" />
            <el-option label="箱线图" value="boxplot" />
          </el-select>
        </el-form-item>

        <!-- 多项式回归阶数 -->
        <el-form-item v-if="queryForm.analysisType === 'polynomial'" label="多项式阶数">
          <el-input-number v-model="queryForm.degree" :min="1" :max="5" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" :disabled="!queryResult" @click="handleAnalyze">统计分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 查询结果和分析结果切换 -->
    <el-tabs v-if="queryResult" v-model="activeTab" class="result-tabs">
      <el-tab-pane label="查询结果" name="query">
        <el-card class="query-result">
          <template #header>
            <div class="card-header">
              <div class="left-section">
                <span class="title">查询结果</span>
                <el-radio-group v-model="queryForm.chartType" class="chart-type-select">
                  <el-radio-button label="line">折线图</el-radio-button>
                  <el-radio-button label="bar">柱状图</el-radio-button>
                  <el-radio-button label="pie">饼图</el-radio-button>
                </el-radio-group>
              </div>
              <el-button type="primary" @click="exportData">导出数据</el-button>
            </div>
          </template>
          
          <!-- 图表展示 -->
          <div class="chart-wrapper">
            <div class="chart-container" ref="chartRef"></div>
          </div>

          <!-- 数据表格 -->
          <el-table 
            :data="tableData" 
            style="width: 100%; margin-top: 20px;"
            :border="true"
            stripe
            highlight-current-row
            height="400"
          >
            <el-table-column 
              v-if="queryForm.queryType === 'geography'" 
              prop="quarter" 
              label="季度"
              align="center"
              min-width="120"
              fixed
            />
            <el-table-column 
              v-if="queryForm.queryType === 'quarter'" 
              prop="geography" 
              label="地区"
              align="center"
              min-width="120"
              fixed
            />
            <el-table-column 
              prop="population" 
              label="人口数量"
              align="center"
              min-width="120"
              :formatter="(row) => formatNumber(row.population)"
            />
          </el-table>

          <!-- 基础统计信息 -->
          <div class="statistics-info">
            <h3>基础统计信息</h3>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="最大值">{{ formatNumber(basicStats.max) }}</el-descriptions-item>
              <el-descriptions-item label="最小值">{{ formatNumber(basicStats.min) }}</el-descriptions-item>
              <el-descriptions-item label="平均值">{{ formatNumber(basicStats.mean) }}</el-descriptions-item>
              <el-descriptions-item label="中位数">{{ formatNumber(basicStats.median) }}</el-descriptions-item>
              <el-descriptions-item label="标准差">{{ formatNumber(basicStats.stdDev) }}</el-descriptions-item>
              <el-descriptions-item label="总计">{{ formatNumber(basicStats.sum) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="统计分析" name="analysis" v-if="analysisResult">
        <el-card class="analysis-result">
          <template #header>
            <div class="card-header">
              <span class="title">{{ getAnalysisTypeText() }}</span>
            </div>
          </template>
          
          <!-- 图表展示 -->
          <div class="chart-wrapper">
            <div class="chart-container" ref="analysisChartRef"></div>
          </div>

          <!-- 数据结果 -->
          <div class="result-details">
            <el-descriptions :column="2" border>
              <template v-if="queryForm.analysisType === 'linear'">
                <el-descriptions-item label="斜率">{{ analysisResult.slope?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="截距">{{ analysisResult.intercept?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="R方值">{{ analysisResult.r_squared?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="相关系数">{{ analysisResult.correlation?.toFixed(4) }}</el-descriptions-item>
              </template>

              <template v-if="queryForm.analysisType === 'normal'">
                <el-descriptions-item label="均值">{{ analysisResult.mean?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="标准差">{{ analysisResult.standard_deviation?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="P值">{{ analysisResult.p_value?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="是否符合正态分布">{{ analysisResult.is_normal ? '是' : '否' }}</el-descriptions-item>
              </template>

              <template v-if="queryForm.analysisType === 'binomial'">
                <el-descriptions-item label="试验次数">{{ analysisResult.trials }}</el-descriptions-item>
                <el-descriptions-item label="成功概率">{{ analysisResult.probability?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="期望值">{{ analysisResult.expected_mean?.toFixed(4) }}</el-descriptions-item>
                <el-descriptions-item label="方差">{{ analysisResult.expected_variance?.toFixed(4) }}</el-descriptions-item>
              </template>

              <template v-if="queryForm.analysisType === 'boxplot'">
                <el-descriptions-item label="最小值">{{ formatNumber(analysisResult.min) }}</el-descriptions-item>
                <el-descriptions-item label="Q1">{{ formatNumber(analysisResult.q1) }}</el-descriptions-item>
                <el-descriptions-item label="中位数">{{ formatNumber(analysisResult.median) }}</el-descriptions-item>
                <el-descriptions-item label="Q3">{{ formatNumber(analysisResult.q3) }}</el-descriptions-item>
                <el-descriptions-item label="最大值">{{ formatNumber(analysisResult.max) }}</el-descriptions-item>
                <el-descriptions-item label="IQR">{{ formatNumber(analysisResult.iqr) }}</el-descriptions-item>
              </template>
            </el-descriptions>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick, watch } from 'vue';
import { statisticsApi, populationApi } from '../api';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';
import * as XLSX from 'xlsx';

// 查询表单
const queryForm = ref({
  queryType: 'geography',
  geography: '',
  quarter: '',
  chartType: 'line',
  analysisType: '',
  degree: 2
});

// 数据状态
const geographyOptions = ref([]);
const queryResult = ref(null);
const analysisResult = ref(null);
const chartRef = ref(null);
let chart = null;

// 添加分析图表的引用
const analysisChartRef = ref(null);
let analysisChart = null;

// 格式化季度显示
const formatQuarter = (quarter) => {
  const [q, year] = quarter.split(' ');
  return `${year}年${q.replace('Q', '')}季度`;
};

// 生成季度选项
const years = Array.from({ length: 2024 - 1991 + 1 }, (_, i) => 1991 + i).reverse(); // 年份倒序
const quarters = ['Q1', 'Q2', 'Q3', 'Q4'];
const quarterOptions = computed(() => {
  const options = [];
  for (const year of years) {
    for (const quarter of quarters) {
      const value = `${quarter} ${year}`;
      options.push({
        label: formatQuarter(value),
        value: value
      });
    }
  }
  return options;
});

// 计算属性
const tableData = computed(() => {
  if (!queryResult.value) return [];
  
  if (queryForm.value.queryType === 'geography') {
    // 获取所有季度并排序
    return Object.entries(queryResult.value.quarterData || {})
      .sort(([quarterA], [quarterB]) => {
        const [qA, yearA] = quarterA.split(' ');
        const [qB, yearB] = quarterB.split(' ');
        const yearDiff = parseInt(yearA) - parseInt(yearB);
        if (yearDiff !== 0) return yearDiff;
        return parseInt(qA.slice(1)) - parseInt(qB.slice(1));
      })
      .map(([quarter, population]) => ({
        quarter: formatQuarter(quarter),
        population: population
      }));
  } else {
    return queryResult.value.map(item => ({
      geography: item.geography,
      population: item.population
    }));
  }
});

// 基础统计计算
const basicStats = computed(() => {
  if (!queryResult.value) return {};

  const populations = queryForm.value.queryType === 'geography'
    ? Object.values(queryResult.value.quarterData || {})
    : queryResult.value.map(item => item.population);

  const sorted = [...populations].sort((a, b) => a - b);
  const sum = populations.reduce((a, b) => a + b, 0);
  const mean = sum / populations.length;
  const median = populations.length % 2 === 0
    ? (sorted[populations.length / 2 - 1] + sorted[populations.length / 2]) / 2
    : sorted[Math.floor(populations.length / 2)];
  const stdDev = Math.sqrt(
    populations.reduce((acc, val) => acc + Math.pow(val - mean, 2), 0) / populations.length
  );

  return {
    max: Math.max(...populations),
    min: Math.min(...populations),
    mean,
    median,
    stdDev,
    sum
  };
});

// 添加活动标签状态
const activeTab = ref('query');

// 初始化
onMounted(async () => {
  try {
    // 获取地区列表
    const response = await populationApi.getAllGeographies();
    geographyOptions.value = response.data;
    
    // 初始化图表
    if (chartRef.value) {
      chart = echarts.init(chartRef.value);
    }
    if (analysisChartRef.value) {
      analysisChart = echarts.init(analysisChartRef.value);
    }
  } catch (error) {
    console.error('初始化失败:', error);
    ElMessage.error('初始化失败，请刷新页面重试');
  }
});

// 处理查询
const handleQuery = async () => {
  const { queryType, geography, quarter } = queryForm.value;
  if ((queryType === 'geography' && !geography) || (queryType === 'quarter' && !quarter)) {
    ElMessage.warning('请选择查询条件');
    return;
  }

  try {
    let response;
    if (queryType === 'geography') {
      response = await populationApi.getByGeography(geography);
      queryResult.value = response.data;
    } else {
      response = await populationApi.getByQuarter(quarter);
      queryResult.value = response.data;
    }
    
    // 确保图表容器存在
    nextTick(() => {
      if (!chart && chartRef.value) {
        chart = echarts.init(chartRef.value);
      }
      if (chart) {
        updateChart();
      }
    });

    // 重置分析结果和切换到查询结果标签
    analysisResult.value = null;
    activeTab.value = 'query';
    
    // 销毁旧的分析图表实例
    if (analysisChart) {
      analysisChart.dispose();
      analysisChart = null;
    }
    
    ElMessage.success('查询成功');
  } catch (error) {
    console.error('查询失败:', error);
    ElMessage.error('查询失败，请重试');
  }
};

// 更新图表
const updateChart = () => {
  if (!chart || !queryResult.value) return;

  const option = {
    title: {
      text: getChartTitle(),
      left: 'center',
      top: 10
    },
    tooltip: {
      trigger: queryForm.value.chartType === 'pie' ? 'item' : 'axis',
      formatter: queryForm.value.chartType === 'pie' 
        ? '{b}: {c} ({d}%)'
        : (params) => {
          if (Array.isArray(params)) {
            return params[0].name + ': ' + formatNumber(params[0].value);
          }
          return params.name + ': ' + formatNumber(params.value);
        }
    },
    legend: queryForm.value.chartType === 'pie' ? {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 50,
      bottom: 20,
      pageButtonPosition: 'end',
      formatter: (name) => {
        // 限制图例文字长度
        return name.length > 12 ? name.substring(0, 12) + '...' : name;
      }
    } : undefined,
    grid: {
      left: '5%',
      right: queryForm.value.chartType === 'pie' ? '15%' : '5%',
      bottom: '15%',
      containLabel: true
    },
    dataZoom: queryForm.value.queryType === 'geography' && queryForm.value.chartType !== 'pie' ? [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: 25
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: 25
      }
    ] : undefined
  };

  const xAxisData = getXAxisData();
  const seriesData = getSeriesData();

  // 根据图表类型设置不同的配置
  if (queryForm.value.chartType === 'pie') {
    const pieData = xAxisData.map((name, index) => ({
      name,
      value: seriesData[index]
    }));

    // 根据数据量调整饼图配置
    const dataCount = pieData.length;
    const isLargeData = dataCount > 20;

    option.series = [{
      name: queryForm.value.queryType === 'geography' ? '季度人口' : '地区人口',
      type: 'pie',
      radius: isLargeData ? ['20%', '60%'] : ['40%', '70%'],
      center: ['40%', '50%'],
      roseType: isLargeData ? 'area' : false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: !isLargeData,
        position: 'outside',
        formatter: '{b}\n{c} ({d}%)',
        fontSize: 10,
        alignTo: 'edge',
        edgeDistance: '10%',
        distanceToLabelLine: 5
      },
      labelLine: {
        show: !isLargeData,
        length: 10,
        length2: 10
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 12,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: pieData.sort((a, b) => b.value - a.value) // 按值大小排序
    }];
  } else {
    option.xAxis = {
      type: 'category',
      data: xAxisData,
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    };
    option.yAxis = {
      type: 'value',
      name: '人口数量',
      nameLocation: 'middle',
      nameGap: 50,
      axisLabel: {
        formatter: (value) => formatNumber(value)
      }
    };
    option.series = [{
      name: queryForm.value.queryType === 'geography' ? '季度人口' : '地区人口',
      type: queryForm.value.chartType,
      data: seriesData,
      smooth: queryForm.value.chartType === 'line',
      label: {
        show: false
      },
      markPoint: {
        data: [
          { type: 'max', name: '最大值' },
          { type: 'min', name: '最小值' }
        ]
      },
      markLine: {
        data: [
          { type: 'average', name: '平均值' }
        ]
      }
    }];
  }

  nextTick(() => {
    chart.setOption(option, true);
  });
};

// 获取图表标题
const getChartTitle = () => {
  if (queryForm.value.queryType === 'geography') {
    return `${queryForm.value.geography} 各季度人口数据`;
  } else {
    return `${queryForm.value.quarter} 各地区人口数据`;
  }
};

// 获取X轴数据
const getXAxisData = () => {
  if (!queryResult.value) return [];
  
  if (queryForm.value.queryType === 'geography') {
    // 获取所有季度并排序
    return Object.entries(queryResult.value.quarterData || {})
      .sort(([quarterA], [quarterB]) => {
        const [qA, yearA] = quarterA.split(' ');
        const [qB, yearB] = quarterB.split(' ');
        const yearDiff = parseInt(yearA) - parseInt(yearB);
        if (yearDiff !== 0) return yearDiff;
        return parseInt(qA.slice(1)) - parseInt(qB.slice(1));
      })
      .map(([quarter]) => formatQuarter(quarter));
  } else {
    return queryResult.value.map(item => item.geography);
  }
};

// 获取系列数据
const getSeriesData = () => {
  if (!queryResult.value) return [];

  if (queryForm.value.queryType === 'geography') {
    // 获取所有季度并排序
    return Object.entries(queryResult.value.quarterData || {})
      .sort(([quarterA], [quarterB]) => {
        const [qA, yearA] = quarterA.split(' ');
        const [qB, yearB] = quarterB.split(' ');
        const yearDiff = parseInt(yearA) - parseInt(yearB);
        if (yearDiff !== 0) return yearDiff;
        return parseInt(qA.slice(1)) - parseInt(qB.slice(1));
      })
      .map(([_, value]) => value);
  } else {
    return queryResult.value.map(item => item.population);
  }
};

// 导出数据
const exportData = () => {
  const data = tableData.value;
  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Population Data');
  XLSX.writeFile(wb, `population_data_${Date.now()}.xlsx`);
};

// 处理统计分析
const handleAnalyze = async () => {
  if (!queryForm.value.analysisType || !queryResult.value) {
    ElMessage.warning('请先进行查询并选择统计分析类型');
    return;
  }

  try {
    const { queryType, geography, quarter, analysisType, degree } = queryForm.value;
    let response;
    const analysisData = queryType === 'geography' 
      ? Object.values(queryResult.value.quarterData || {})
      : queryResult.value.map(item => item.population);

    // 数据预处理：确保数据是数值类型
    const processedData = analysisData.map(Number);
    
    // 检查数据有效性
    if (processedData.some(isNaN)) {
      throw new Error('数据包含无效值');
    }

    switch (analysisType) {
      case 'linear':
        response = await statisticsApi.getLinearRegression(processedData);
        break;
      case 'polynomial':
        // 确保阶数是有效的
        const validDegree = Math.min(Math.max(1, degree), 5);
        response = await statisticsApi.getPolynomialRegression(processedData, validDegree);
        break;
      case 'normal':
        response = await statisticsApi.getNormalDistribution(processedData);
        break;
      case 'binomial':
        response = await statisticsApi.getBinomialDistribution(processedData);
        break;
      case 'boxplot':
        response = await statisticsApi.getBoxPlot(processedData);
        break;
    }

    analysisResult.value = response.data;
    
    // 确保分析图表容器存在并已初始化
    nextTick(() => {
      if (!analysisChart && analysisChartRef.value) {
        analysisChart = echarts.init(analysisChartRef.value);
      }
      if (analysisChart) {
        updateAnalysisChart();
      }
    });

    // 切换到分析结果标签
    activeTab.value = 'analysis';
    
    ElMessage.success('分析完成');
  } catch (error) {
    console.error('分析失败:', error);
    if (error.message === '数据包含无效值') {
      ElMessage.error('数据包含无效值，请检查数据');
    } else {
      ElMessage.error('分析失败，请重试');
    }
  }
};

// 获取分析类型文本
const getAnalysisTypeText = () => {
  const typeMap = {
    'linear': '线性回归分析',
    'polynomial': '多项式回归分析',
    'normal': '正态分布分析',
    'binomial': '二项分布分析',
    'boxplot': '箱线图分析'
  };
  return typeMap[queryForm.value.analysisType] || '统计分析';
};

// 更新统计分析图表
const updateAnalysisChart = () => {
  if (!analysisChart || !analysisResult.value) return;

  // 清除现有图表
  analysisChart.clear();

  const option = {
    title: {
      text: `${queryForm.value.queryType === 'geography' ? queryForm.value.geography : queryForm.value.quarter} - ${getAnalysisTypeText()}`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      containLabel: true
    }
  };

  // 获取原始数据用于绘制散点图
  const rawData = queryForm.value.queryType === 'geography'
    ? Object.values(queryResult.value.quarterData || {})
    : queryResult.value.map(item => item.population);
  const xAxisData = Array.from({ length: rawData.length }, (_, i) => i);

  switch (queryForm.value.analysisType) {
    case 'boxplot':
      option.yAxis = {
        type: 'value',
        name: '人口数量',
        nameLocation: 'middle',
        nameGap: 50,
        axisLabel: {
          formatter: (value) => formatNumber(value)
        }
      };
      option.xAxis = {
        type: 'category',
        data: ['人口分布'],
        nameGap: 30
      };
      option.series = [{
        name: '人口分布',
        type: 'boxplot',
        data: [[
          analysisResult.value.min,
          analysisResult.value.q1,
          analysisResult.value.median,
          analysisResult.value.q3,
          analysisResult.value.max
        ]],
        itemStyle: {
          borderWidth: 2,
          borderColor: '#409EFF'
        }
      }];
      break;
    case 'normal':
      // 生成正态分布曲线点
      const mean = analysisResult.value.mean;
      const std = analysisResult.value.standard_deviation;
      const points = 100;
      const range = 4 * std;
      const x = Array.from({ length: points }, (_, i) => mean - range + (2 * range * i) / (points - 1));
      const y = x.map(val => {
        const z = (val - mean) / std;
        return (1 / (std * Math.sqrt(2 * Math.PI))) * Math.exp(-0.5 * z * z);
      });

      option.xAxis = {
        type: 'value',
        name: '人口数量',
        nameLocation: 'middle',
        nameGap: 30,
        axisLabel: {
          formatter: (value) => formatNumber(value)
        }
      };
      option.yAxis = {
        type: 'value',
        name: '密度',
        nameLocation: 'middle',
        nameGap: 50
      };
      option.series = [
        {
          name: '实际数据',
          type: 'scatter',
          data: rawData.map(value => [value, 0]),
          symbolSize: 8,
          itemStyle: {
            color: '#409EFF'
          }
        },
        {
          name: '正态分布拟合',
          type: 'line',
          data: x.map((val, i) => [val, y[i]]),
          smooth: true,
          lineStyle: {
            width: 2,
            color: '#67C23A'
          }
        }
      ];
      break;
    case 'binomial':
      option.xAxis = {
        type: 'category',
        data: ['试验次数', '成功概率', '期望值', '方差'],
        axisLabel: { interval: 0 }
      };
      option.yAxis = {
        type: 'value',
        name: '数值',
        nameLocation: 'middle',
        nameGap: 50
      };
      option.series = [{
        type: 'bar',
        data: [
          analysisResult.value.trials,
          analysisResult.value.probability,
          analysisResult.value.expected_mean,
          analysisResult.value.expected_variance
        ],
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c:.4f}'
        }
      }];
      break;
    case 'linear':
    case 'polynomial':
      if (!analysisResult.value.coefficients && !analysisResult.value.slope) {
        console.error('回归系数未定义');
        return;
      }

      // 计算拟合曲线
      const fitX = Array.from({ length: 100 }, (_, i) => i * (rawData.length - 1) / 99);
      const fitY = fitX.map(x => {
        if (queryForm.value.analysisType === 'linear') {
          return analysisResult.value.slope * x + analysisResult.value.intercept;
        } else if (analysisResult.value.coefficients) {
          return analysisResult.value.coefficients.reduce((sum, coef, power) => 
            sum + coef * Math.pow(x, power), 0);
        }
        return 0;
      });

      option.xAxis = {
        type: 'value',
        name: '时间',
        nameLocation: 'middle',
        nameGap: 30
      };
      option.yAxis = {
        type: 'value',
        name: '人口数量',
        nameLocation: 'middle',
        nameGap: 50,
        axisLabel: {
          formatter: (value) => formatNumber(value)
        }
      };
      option.series = [
        {
          name: '实际数据',
          type: 'scatter',
          data: xAxisData.map((x, i) => [x, rawData[i]]),
          symbolSize: 8,
          itemStyle: {
            color: '#409EFF'
          }
        },
        {
          name: '拟合曲线',
          type: 'line',
          data: fitX.map((x, i) => [x, fitY[i]]),
          smooth: true,
          lineStyle: {
            width: 2,
            color: '#67C23A'
          }
        }
      ];
      break;
  }

  nextTick(() => {
    analysisChart.setOption(option);
  });
};

// 组件卸载时清理
onUnmounted(() => {
  chart?.dispose();
  analysisChart?.dispose();
  window.removeEventListener('resize', handleResize);
});

// 监听窗口大小变化
const handleResize = () => {
  chart?.resize();
  analysisChart?.resize();
};
window.addEventListener('resize', handleResize);

// 格式化数字显示
const formatNumber = (value) => {
  return new Intl.NumberFormat('zh-CN').format(value);
};

// 监听图表类型变化
watch(() => queryForm.value.chartType, () => {
  if (chart && queryResult.value) {
    updateChart();
  }
});
</script>

<style scoped>
.data-analysis {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
}

.query-form {
  width: 100%;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.statistics-info h3,
.result-details h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  position: relative;
  padding-left: 12px;
}

.statistics-info h3::before,
.result-details h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: linear-gradient(to bottom, #3498db, #2980b9);
  border-radius: 2px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background: linear-gradient(90deg, #f8f9fa 0%, #ffffff 100%);
  font-weight: 600;
}

:deep(.el-table td) {
  transition: all 0.3s ease;
}

:deep(.el-table tr:hover td) {
  background-color: #f5f7fa !important;
}

:deep(.el-descriptions) {
  padding: 16px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-descriptions__title) {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: linear-gradient(90deg, transparent, #dcdfe6, transparent);
}

:deep(.el-tabs__item) {
  font-size: 15px;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  font-weight: 600;
  color: #3498db;
}

:deep(.el-button) {
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #3498db, #2980b9);
  border: none;
  box-shadow: 0 2px 6px rgba(52, 152, 219, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.4);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  border: none;
  box-shadow: 0 2px 6px rgba(46, 204, 113, 0.3);
}

:deep(.el-button--success:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(46, 204, 113, 0.4);
}

:deep(.el-select),
:deep(.el-input),
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input__inner),
:deep(.el-select__input) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus),
:deep(.el-select__input:focus) {
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-radio-group) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}

:deep(.el-radio) {
  margin-right: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.query-result,
.analysis-result {
  animation: fadeIn 0.5s ease-out;
}
</style> 