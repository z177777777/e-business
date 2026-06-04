import http from "./http";

export const checkoutOrder = () => http.post("/api/orders/checkout");
export const listOrders = () => http.get("/api/orders");
export const getOrder = (id) => {
	const isAdmin = typeof window !== 'undefined' && window.location && window.location.pathname && window.location.pathname.startsWith('/admin');
	const base = isAdmin ? '/api/admin/orders' : '/api/orders';
	return http.get(`${base}/${id}`);
};
export const payOrder = (id) => http.post(`/api/orders/${id}/pay`);
export const receiveOrder = (id) => http.post(`/api/orders/${id}/receive`);
export const cancelOrder = (id) => http.post(`/api/orders/${id}/cancel`);
export const requestRefund = (id) => http.post(`/api/orders/${id}/refund`);
