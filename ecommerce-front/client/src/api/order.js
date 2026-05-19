import http from "./http";

export const checkoutOrder = () => http.post("/api/orders/checkout");
export const listOrders = () => http.get("/api/orders");
export const getOrder = (id) => http.get(`/api/orders/${id}`);
export const payOrder = (id) => http.post(`/api/orders/${id}/pay`);
