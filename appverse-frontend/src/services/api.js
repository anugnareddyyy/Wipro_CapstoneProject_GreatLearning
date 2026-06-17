import axios from 'axios';

/**
 * Configured Axios instance for all AppVerse API calls.
 * 
 * Features:
 * - Auto-attaches JWT Bearer token from localStorage
 * - Auto-redirects to login on 401 Unauthorized
 * - Consistent base URL from environment
 */
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

/** Request interceptor: attach JWT token to every authenticated request */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('appverse_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/** Response interceptor: handle auth errors globally */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — clear storage and redirect to login
      localStorage.removeItem('appverse_token');
      localStorage.removeItem('appverse_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

// ===== AUTH =====
export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// ===== APPS =====
export const appService = {
  getAll: (params) => api.get('/apps', { params }),
  search: (params) => api.get('/apps/search', { params }),
  getById: (id) => api.get(`/apps/${id}`),
  getFeatured: () => api.get('/apps/featured'),
  getTrending: (limit = 10) => api.get('/apps/trending', { params: { limit } }),
  getTopRated: (limit = 10) => api.get('/apps/top-rated', { params: { limit } }),
  getSimilar: (id, limit = 6) => api.get(`/apps/${id}/similar`, { params: { limit } }),
  create: (data) => api.post('/apps', data),
  update: (id, data) => api.put(`/apps/${id}`, data),
  delete: (id) => api.delete(`/apps/${id}`),
  getMyApps: () => api.get('/apps/my-apps'),
  recordDownload: (id, platform, country) =>
    api.post(`/apps/${id}/download`, null, { params: { platform, country } }),
  // Admin
  getPending: () => api.get('/apps/admin/pending'),
  updateStatus: (id, status) => api.patch(`/apps/${id}/status`, null, { params: { status } }),
  toggleFeatured: (id) => api.patch(`/apps/${id}/featured`),
};

// ===== REVIEWS =====
export const reviewService = {
  getForApp: (appId, params) => api.get(`/reviews/app/${appId}`, { params }),
  submit: (appId, data) => api.post(`/reviews/app/${appId}`, data),
  update: (reviewId, data) => api.put(`/reviews/${reviewId}`, data),
  delete: (reviewId) => api.delete(`/reviews/${reviewId}`),
  getSentiment: (appId) => api.get(`/reviews/app/${appId}/sentiment`),
  getMyReviews: () => api.get('/reviews/my-reviews'),
  getFlagged: () => api.get('/reviews/admin/flagged'),
  toggleVisibility: (reviewId) => api.patch(`/reviews/${reviewId}/visibility`),
};

// ===== RECOMMENDATIONS (AI) =====
export const recommendationService = {
  getForMe: (limit = 10) => api.get('/recommendations/for-me', { params: { limit } }),
  getTrending: () => api.get('/recommendations/trending'),
  predictDownloads: (appId) => api.get(`/recommendations/apps/${appId}/download-prediction`),
  getCategoryInsights: () => api.get('/recommendations/category-insights'),
};
