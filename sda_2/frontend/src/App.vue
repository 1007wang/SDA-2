<template>
  <div class="app">
    <el-container>
      <el-header>
        <h1>人口数据统计分析系统</h1>
      </el-header>
      <el-main>
        <DataAnalysis />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import DataAnalysis from './components/DataAnalysis.vue'
</script>

<style>
.app {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.el-header {
  background-color: #fff;
  color: #303133;
  text-align: center;
  line-height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12);
}

.el-main {
  padding: 20px;
}

h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
</style> 