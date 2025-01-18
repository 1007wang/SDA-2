import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:7090/api',
    timeout: 10000,
});

// 统计分析相关接口
export const statisticsApi = {
    // 线性回归分析
    getLinearRegression: (geography) => 
        api.get(`/statistics/linear-regression/${geography}`),
    
    // 多项式回归分析
    getPolynomialRegression: (geography, degree = 2) => 
        api.get(`/statistics/polynomial-regression/${geography}`, { params: { degree } }),
    
    // 正态分布拟合
    getNormalDistribution: (geography) => 
        api.get(`/statistics/normal-distribution/${geography}`),
    
    // 二项分布拟合
    getBinomialDistribution: (geography) => 
        api.get(`/statistics/binomial-distribution/${geography}`),
    
    // 箱线图数据
    getBoxPlot: (geography) => 
        api.get(`/statistics/box-plot/${geography}`),
}; 