import http from "./http";

export const submitFeedback = (data) => {
  return http.post("/api/feedback", data);
};
