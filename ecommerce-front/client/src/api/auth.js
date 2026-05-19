import http from "./http";

export const sendCode = (email, purpose) => {
  return http.post("/api/auth/send-code", { email, purpose });
};

export const register = (payload) => {
  return http.post("/api/auth/register", payload);
};

export const login = (payload) => {
  return http.post("/api/auth/login", payload);
};

export const resetPassword = (payload) => {
  return http.post("/api/auth/reset-password", payload);
};

export const logout = () => {
  return http.post("/api/auth/logout");
};
