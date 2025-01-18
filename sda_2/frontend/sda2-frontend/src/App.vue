<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="app-title">人口数据分析系统</h1>
          <span class="app-subtitle">Population Data Analysis System</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">
            <el-icon><House /></el-icon>
            首页
          </router-link>
          <router-link to="/analysis" class="nav-item">
            <el-icon><DataAnalysis /></el-icon>
            数据分析
          </router-link>
          <a href="https://github.com/yourusername/your-repo" target="_blank" class="nav-item">
            <el-icon><Link /></el-icon>
            项目源码
          </a>
        </nav>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-content">
        <p>© 2024 人口数据分析系统. All rights reserved.</p>
        <p>Powered by Vue.js & Element Plus</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { House, DataAnalysis, Link } from '@element-plus/icons-vue'
</script>

<style>
:root {
  --primary-color: #3498db;
  --secondary-color: #2c3e50;
  --background-color: #f5f7fa;
  --text-color: #2c3e50;
  --border-color: #ebeef5;
  --header-height: 64px;
  --footer-height: 60px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-color);
  background: var(--background-color);
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  width: 100%;
}

.app-header {
  height: var(--header-height);
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  flex-direction: column;
}

.app-title {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(120deg, var(--secondary-color), var(--primary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.app-subtitle {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.nav-menu {
  display: flex;
  gap: 24px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-color);
  text-decoration: none;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: #e8f3fb;
  color: var(--primary-color);
}

.nav-item.router-link-active {
  background: #e8f3fb;
  color: var(--primary-color);
  font-weight: 600;
}

.app-main {
  margin-top: var(--header-height);
  flex: 1;
  width: 100%;
  min-height: calc(100vh - var(--header-height) - var(--footer-height));
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.app-main > :deep(*) {
  flex: 1;
  width: 100%;
  height: 100%;
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
}

.app-footer {
  height: var(--footer-height);
  background: #ffffff;
  border-top: 1px solid var(--border-color);
  width: 100%;
}

.footer-content {
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  padding: 12px 24px;
  text-align: center;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .header-content,
  .app-main > :deep(*),
  .footer-content {
    padding: 16px;
  }

  .app-main {
    margin-top: calc(var(--header-height) + 48px);
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>
