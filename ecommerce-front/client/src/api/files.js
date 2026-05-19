import http from "./http";

export const uploadImage = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return http.post("/api/files/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
};
