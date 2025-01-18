import { createRouter, createWebHistory } from 'vue-router'
import DataAnalysis from '../components/DataAnalysis.vue'

const routes = [
  {
    path: '/',
    redirect: '/analysis'
  },
  {
    path: '/analysis',
    name: 'analysis',
    component: DataAnalysis,
    meta: {
      title: '数据分析'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 人口数据分析系统` : '人口数据分析系统'
  next()
})

export default router 