import http from "./http";

export const getProfile = () => {
  return http.get("/api/users/me");
};

export const updateProfile = (payload) => {
  return http.put("/api/users/me/profile", payload);
};

export const updateEmail = (payload) => {
  return http.put("/api/users/me/email", payload);
};

export const updatePassword = (payload) => {
  return http.put("/api/users/me/password", payload);
};
