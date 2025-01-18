import axios from 'axios';

const API_BASE_URL = 'http://localhost:7090/api';

export default {
  // 基础数据查询接口
  getAllData() {
    return axios.get(`${API_BASE_URL}/population/all`);
  },
  getPopulationByGeography(geography) {
    return axios.get(`${API_BASE_URL}/population/by-geography/${geography}`);
  },
  getPopulationByQuarter(quarter) {
    return axios.get(`${API_BASE_URL}/population/by-quarter/${quarter}`);
  },

  // 统计分析接口
  getLinearRegression(geography) {
    return axios.get(`${API_BASE_URL}/statistics/linear-regression/${geography}`);
  },
  getPolynomialRegression(geography, degree = 2) {
    return axios.get(`${API_BASE_URL}/statistics/polynomial-regression/${geography}`, {
      params: { degree }
    });
  },
  getNormalDistribution(geography) {
    return axios.get(`${API_BASE_URL}/statistics/normal-distribution/${geography}`);
  },
  getBinomialDistribution(geography) {
    return axios.get(`${API_BASE_URL}/statistics/binomial-distribution/${geography}`);
  },
  getBoxPlotStats(geography) {
    return axios.get(`${API_BASE_URL}/statistics/box-plot/${geography}`);
  }
};
