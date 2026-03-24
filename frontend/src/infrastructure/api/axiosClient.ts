import axios from 'axios';
import { isProblemDetail, formatProblemDetail } from './problemDetail';

const AUTH_TOKEN_KEY = 'auth_token';

/**
 * Centralized Axios instance.
 * - baseURL from environment variable
 * - timeout: 5s (SRE: prevents hanging requests)
 * - Request interceptor: injects JWT Bearer token
 * - Response interceptor: handles 401 logout and RFC 7807 errors
 */
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: Number(import.meta.env.VITE_REQUEST_TIMEOUT_MS) || 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// === Request Interceptor: JWT Injection ===
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(AUTH_TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// === Response Interceptor: 401 Logout + RFC 7807 Parse ===
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 Unauthorized → clear token and redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem(AUTH_TOKEN_KEY);
      window.location.href = '/login';
      return Promise.reject(new Error('Oturumunuz sona erdi. Lütfen tekrar giriş yapın.'));
    }

    // RFC 7807 Problem Detail format
    if (error.response?.data && isProblemDetail(error.response.data)) {
      const message = formatProblemDetail(error.response.data);
      return Promise.reject(new Error(message));
    }

    // Generic error
    const message = error.response?.data?.message 
      || error.message 
      || 'Sunucuya bağlanılamadı';
    return Promise.reject(new Error(message));
  }
);

export default axiosClient;
