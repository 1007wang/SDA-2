<template>
  <div class="home">
    <h1>人口数据查询与可视化</h1>

    <!-- 查询条件 -->
    <div class="query-section">
      <div class="query-item">
        <label for="geography">按地区查询:</label>
        <select id="geography" v-model="geography" :disabled="isQuarterSelected">
          <option value="" disabled>选择地区</option>
          <option v-for="area in areas" :key="area" :value="area">{{ area }}</option>
        </select>
      </div>

      <div class="query-item">
        <label for="quarter">按季度查询:</label>
        <select id="quarter" v-model="quarter" :disabled="isGeographySelected">
          <option value="" disabled>选择季度</option>
          <option v-for="q in quarters" :key="q.value" :value="q.value">{{ q.label }}</option>
        </select>
      </div>

      <button @click="fetchData" :disabled="loading">
        {{ loading ? '查询中...' : '查询' }}
      </button>

      <!-- 重置按钮 -->
      <button @click="resetForm" :disabled="loading">
        重置
      </button>
    </div>

    <!-- 查询结果展示 -->
    <div v-if="tableData.length">
      <table class="data-table">
        <thead>
          <tr>
            <th v-if="geography">季度</th>
            <th v-if="quarter">地区</th>
            <th>人口数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in tableData" :key="index">
            <td v-if="geography">{{ formatQuarter(row.quarter) }}</td>
            <td v-if="quarter">{{ row.geography }}</td>
            <td>{{ row.population }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="chartData.length">
        <div class="chart-options">
          <label for="chartType">选择图表类型:</label>
          <select id="chartType" v-model="chartType">
            <option value="line">折线图</option>
            <option value="bar">柱状图</option>
            <option value="pie">饼图</option>
          </select>
        </div>
        <v-chart :option="chartOptions" style="height: 500px; width: 100%;"></v-chart>
      </div>

      <!-- 显示统计信息 -->
      <div v-if="statistics" class="statistics-section">
        <h3>统计信息</h3>
        <ul class="statistics-list">
          <li class="stat-item"><strong>均值:</strong> {{ statistics.mean }}</li>
          <li class="stat-item"><strong>众数:</strong> {{ statistics.mode }}</li>
          <li class="stat-item"><strong>中位数:</strong> {{ statistics.median }}</li>
          <li class="stat-item"><strong>标准差:</strong> {{ distribution.stddev }}</li>
          <li class="stat-item"><strong>Q1:</strong> {{ distribution.q1 }}</li>
          <li class="stat-item"><strong>Q3:</strong> {{ distribution.q3 }}</li>
          <li class="stat-item"><strong>IQR:</strong> {{ distribution.iqr }}</li>
          <li class="stat-item"><strong>截距 (Intercept):</strong> {{ regression.intercept }}</li>
          <li class="stat-item"><strong>斜率 (Slope):</strong> {{ regression.slope }}</li>
          <li class="stat-item"><strong>相关系数 (R²):</strong> {{ regression.rSquared }}</li>
        </ul>
      </div>

      <div v-if="!loading && !tableData.length" class="no-data">无查询结果</div>
    </div>
  </div>
</template>



<script>
import { defineComponent } from 'vue';
import { use } from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import { TooltipComponent, LegendComponent, GridComponent, TitleComponent, AxisPointerComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import api from '@/services/api';

use([LineChart, BarChart, PieChart, TooltipComponent, LegendComponent, GridComponent, TitleComponent, AxisPointerComponent]);

export default defineComponent({
  name: 'Home',
  components: { VChart },
  data() {
    return {
      geography: '',
      quarter: '',
      areas: [
        'Canada', 'Newfoundland and Labrador', 'Prince Edward Island', 'Nova Scotia',
        'New Brunswick', 'Quebec', 'Ontario', 'Manitoba', 'Saskatchewan', 'Alberta',
        'British Columbia', 'Yukon', 'Northwest Territories', 'Nunavut'
      ],
      quarters: [],
      tableData: [],
      chartData: [],
      chartType: 'line',
      loading: false,
      statistics: null,  // 新增字段用于保存统计信息
      distribution: null,  // 新增字段用于保存人口分布信息
      regression: null,  // 保存回归分析结果
    };
  },
  created() {
    const quarterKeys = ['q3_2023', 'q4_2023', 'q1_2024', 'q2_2024', 'q3_2024'];
    this.quarters = quarterKeys
      .map((key) => {
        const match = key.match(/q(\d)_(\d{4})/);
        return match ? { label: `${match[2]}年第${match[1]}季度`, value: key } : null;
      })
      .filter((item) => item !== null)
      .sort((a, b) => {
        const yearDiff = parseInt(a.value.split('_')[1]) - parseInt(b.value.split('_')[1]);
        if (yearDiff !== 0) return yearDiff;
        return parseInt(a.value.split('_')[0].substring(1)) - parseInt(b.value.split('_')[0].substring(1));
      });
  },
  computed: {
    isGeographySelected() {
      return this.geography !== '';
    },
    isQuarterSelected() {
      return this.quarter !== '';
    },
    chartOptions() {
      if (!this.chartData || !this.chartData.length) {
        return {};
      }

      const colors = ['#5470C6', '#91CC75', '#FF9F7F', '#FF6347', '#00BFFF'];

      const options = {
        title: {
          text: '人口数据统计',
          subtext: '按地区和季度',
          left: 'center',
        },
        tooltip: {
          trigger: 'item',
        },
        legend: {
          data: this.chartData.map((item) => item.name),
          bottom: 10,
        },
        series: [
          {
            name: '人口数',
            type: this.chartType,
            data: this.chartData.map((item, index) => ({
              value: item.value,
              name: item.name,
              itemStyle: {
                color: colors[index % colors.length],
              },
            })),
            colorBy: 'series',
          },
        ],
      };

      if (this.chartType === 'pie') {
        options.series[0].radius = '50%';
        options.series[0].label = {
          formatter: '{b}: {d}%',
        };
        options.series[0].labelLine = {
          length: 10,
        };
      } else {
        options.xAxis = {
          type: 'category',
          data: this.chartData.map(item => item.name),
          axisLabel: {
            rotate: 45,
            interval: 0,
          },
        };
        options.yAxis = {
          type: 'value',
        };
      }

      return options;
    },
  },
  methods: {
    async fetchData() {
      if (!this.geography && !this.quarter) {
        alert('请至少选择地区或季度！');
        return;
      }

      this.loading = true;
      try {
        let response;
        if (this.geography && this.quarter) {
          // 获取人口数据
          response = await api.getPopulationByGeographyAndQuarter(this.geography, this.quarter);
          this.tableData = [{
            geography: this.geography,
            quarter: this.quarter,
            population: response.data.population
          }];
          this.chartData = this.processChartData(response.data);

          // 获取统计数据
          const stats = await api.getMeanModeMedian({ geography: this.geography, quarter: this.quarter });
          this.statistics = stats.data;

          // 获取人口分布数据
          const distribution = await api.getPopulationDistribution({ geography: this.geography, quarter: this.quarter });
          this.distribution = distribution.data;

          const regression = await api.getLinearRegression({ geography: this.geography, quarter: this.quarter });
          this.regression = regression.data
          console.log(this.distribution);


        } else if (this.geography) {
          // 按地区查询
          response = await api.getPopulationByGeography(this.geography);
          this.tableData = Object.entries(response.data)
            .filter(([key]) => key.startsWith('q'))
            .map(([key, value]) => ({
              geography: this.geography,
              quarter: key,
              population: value
            }));
          this.chartData = this.processChartData(response.data);

          // 获取统计数据
          const stats = await api.getMeanModeMedian({ geography: this.geography });
          this.statistics = stats.data;

          // 获取人口分布数据
          const distribution = await api.getPopulationDistribution({ geography: this.geography });
          this.distribution = distribution.data;

          const regression = await api.getLinearRegression({ geography: this.geography });
          this.regression = regression.data

        } else if (this.quarter) {
          // 按季度查询
          response = await api.getPopulationByQuarter(this.quarter);
          this.tableData = response.data.map(row => ({
            ...row,
            geography: row.geography || '未知地区',
          }));
          this.chartData = this.processChartData(this.tableData);

          // 获取统计数据
          const stats = await api.getMeanModeMedian({ quarter: this.quarter });
          this.statistics = stats.data;

          // 获取人口分布数据
          const distribution = await api.getPopulationDistribution({ quarter: this.quarter });
          this.distribution = distribution.data;

          const regression = await api.getLinearRegression({ quarter: this.quarter });
          this.regression = regression.data
        }

        console.log('Table Data:', this.tableData);
        console.log('Chart Data:', this.chartData);
        console.log('Statistics:', this.statistics);
        console.log('Population Distribution:', this.distribution);
      } catch (error) {
        console.error('查询失败:', error);
        alert('查询失败，请稍后重试！');
      } finally {
        this.loading = false;
      }
    },


    resetForm() {
      this.geography = '';
      this.quarter = '';
      this.tableData = [];
      this.chartData = [];
    },

    processChartData(data) {
      if (Array.isArray(data)) {
        return data.map(row => ({
          name: row.geography,
          value: row.population || 0,
        }));
      } else {
        return Object.entries(data)
          .filter(([key]) => key.startsWith('q'))
          .map(([key, value]) => {
            const match = key.match(/q(\d)_(\d{4})/);
            return match ? { name: `${match[2]}年第${match[1]}季度`, value } : null;
          })
          .filter(item => item !== null);
      }
    },

    formatQuarter(quarter) {
      const match = quarter.match(/q(\d)_(\d{4})/);
      if (match) {
        const [_, quarterNum, year] = match;
        return `${year}年第${quarterNum}季度`;
      }
      return quarter;
    },
  },
});
</script>



<style scoped>
/* 整体页面布局和美化 */
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f8fafc;
  min-height: 100vh;
  border-radius: 15px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 40px;
  font-size: 2.2em;
  font-weight: 600;
  position: relative;
}

h1::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, #4a90e2, #67b0ff);
  border-radius: 2px;
}

/* 查询部分美化 */
.query-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.query-item {
  flex: 1;
  min-width: 250px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.query-item label {
  font-weight: 500;
  color: #2c3e50;
  min-width: 100px;
}

select {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  transition: all 0.3s;
}

select:hover:not(:disabled) {
  border-color: #409eff;
}

select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

button {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

button:first-of-type {
  background: linear-gradient(135deg, #4a90e2, #67b0ff);
  color: white;
}

button:last-of-type {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
}

button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 表格部分美化 */
.data-table {
  width: 100%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin: 30px 0;
}

.data-table th,
.data-table td {
  padding: 15px;
  text-align: center;
  color: #606266;
  border-bottom: 1px solid #e0e0e0;
  /* 行之间的分隔线颜色 */
}

.data-table th {
  border-right: 1px solid #e0e0e0;
  /* 表头与内容之间的分隔线颜色 */
}

.data-table td {
  padding: 15px;
  color: #606266;
  border-right: 1px solid #e0e0e0;
  /* 两列之间的分隔线颜色 */
}

.data-table tr:hover {
  background-color: #f5f7fa;
  /* 行悬停背景色 */
}

/* 如果不想要最后一列有右边框，可以通过以下规则去除 */
.data-table td:last-child {
  border-right: none;
}



/* 图表选项美化 */
.chart-options {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin: 30px 0;
  display: flex;
  align-items: center;
  gap: 15px;
}

.chart-options label {
  font-weight: 500;
  color: #2c3e50;
}

/* 无数据提示美化 */
.no-data {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.statistics-section {
  background: linear-gradient(145deg, #f6f8fb 0%, #e9f1f8 100%);
  border-radius: 12px;
  padding: 25px;
  margin-top: 30px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.statistics-section h3 {
  font-size: 1.6em;
  color: #2c3e50;
  margin-bottom: 20px;
  text-align: center;
  font-weight: 600;
  position: relative;
}

.statistics-section h3::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #4a90e2, #67b0ff);
  border-radius: 2px;
}

.statistics-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  padding: 0;
  list-style: none;
}

.stat-item {
  background: white;
  padding: 15px 20px;
  border-radius: 10px;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
  min-height: 60px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.stat-item strong {
  color: #4a90e2;
  font-weight: 600;
  font-size: 0.9em;
  white-space: nowrap;
}

.stat-item span {
  color: #606266;
  font-size: 1.1em;
  word-break: break-all;
  line-height: 1.4;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border-color: rgba(74, 144, 226, 0.2);
}

/* 响应式布局优化 */
@media (max-width: 768px) {
  .statistics-list {
    grid-template-columns: 1fr;
  }
  
  .stat-item {
    padding: 12px 16px;
  }
}
</style>
