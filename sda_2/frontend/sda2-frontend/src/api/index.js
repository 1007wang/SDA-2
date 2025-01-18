import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:7090',
    timeout: 10000,
    withCredentials: true
});

// 人口数据查询接口
export const populationApi = {
    // 获取所有地区列表
    getAllGeographies: () => 
        api.get('/api/population/geographies'),
    
    // 根据地区查询人口数据
    getByGeography: (geography) => 
        api.get(`/api/population/geography/${geography}`),
    
    // 根据季度查询人口数据
    getByQuarter: (quarter) => 
        api.get(`/api/population/quarter/${quarter}`),
};

// 统计分析相关接口
export const statisticsApi = {
    // 线性回归分析
    getLinearRegression: (data) => {
        return api.post('/api/statistics/linear-regression', { data });
    },
    
    // 多项式回归分析
    getPolynomialRegression: (data, degree) => {
        return api.post('/api/statistics/polynomial-regression', { data, degree });
    },
    
    // 正态分布分析
    getNormalDistribution: (data) => {
        return api.post('/api/statistics/normal-distribution', { data });
    },
    
    // 二项分布分析
    getBinomialDistribution: (data) => {
        return api.post('/api/statistics/binomial-distribution', { data });
    },
    
    // 箱线图分析
    getBoxPlot: (data) => {
        return api.post('/api/statistics/box-plot', { data });
    }
}; 