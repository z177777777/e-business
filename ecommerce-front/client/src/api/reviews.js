import http from "./http";

export const listProductReviews = (idOrSlug) => http.get(`/api/products/${idOrSlug}/reviews`);
export const submitProductReview = (idOrSlug, payload) => http.post(`/api/products/${idOrSlug}/reviews`, payload);
