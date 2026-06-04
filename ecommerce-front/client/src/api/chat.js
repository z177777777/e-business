import http from "./http";

export const getSessions = () => {
  return http.get("/api/chat/sessions");
};

export const getMessages = (targetUserId, before, size = 30) => {
  const params = { targetUserId, size };
  if (before) params.before = before;
  return http.get("/api/chat/messages", { params });
};

export const uploadChatFile = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return http.post("/api/chat/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
};
