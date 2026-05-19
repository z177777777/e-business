import axios from "axios";
import { ElMessage } from "element-plus";

const TOKEN_KEY = "eb_token";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.code !== 0) {
      ElMessage.error(payload.message || "请求失败");
      return Promise.reject(new Error(payload.message || "请求失败"));
    }
    return payload;
  },
  (error) => {
    const status = error?.response?.status;
    const message = error?.response?.data?.message || "请求失败";
    if (status === 401 || status === 403) {
      localStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(TOKEN_KEY);
      ElMessage.error("未登录或会话已过期，请重新登录");
      try {
        window.location.href = "/login";
      } catch (e) {
        // ignore
      }
      return Promise.reject(error);
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default http;
