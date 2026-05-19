import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/store/auth";

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/home", component: () => import("@/views/Home.vue") },
  { path: "/category/:name", component: () => import("@/views/CategoryProducts.vue") },
  { path: "/product/:id", component: () => import("@/views/ProductDetail.vue") },
  { path: "/products", component: () => import("@/views/ProductsList.vue") },
  {
    path: "/cart",
    component: () => import("@/views/Cart.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/orders",
    component: () => import("@/views/Orders.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/orders/:id",
    component: () => import("@/views/OrdersDetail.vue"),
    meta: { requiresAuth: true }
  },
  { path: "/login", component: () => import("@/views/Login.vue") },
  { path: "/register", component: () => import("@/views/Register.vue") },
  { path: "/forgot", component: () => import("@/views/ForgotPassword.vue") },
  {
    path: "/profile",
    component: () => import("@/views/Profile.vue"),
    meta: { requiresAuth: true }
  }
  ,{ path: "/checkout", component: () => import("@/views/Checkout.vue"), meta: { requiresAuth: true } }
    ,
    // Admin routes (reuse user login view)
    { path: "/admin/login", component: () => import("@/views/Login.vue") },
    {
      path: "/admin",
      component: () => import("@/views/admin/AdminLayout.vue"),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: "", redirect: "/admin/dashboard" },
        { path: "dashboard", component: () => import("@/views/admin/AdminDashboard.vue") },
        { path: "users", component: () => import("@/views/admin/AdminUsers.vue") },
        { path: "products", component: () => import("@/views/admin/AdminProducts.vue") },
        { path: "orders", component: () => import("@/views/admin/AdminOrders.vue") }
      ]
    }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.requiresAuth && !auth.token) {
    return "/login";
  }
  if (to.meta.requiresAdmin) {
    if (!auth.user || auth.user.role !== "ADMIN") {
      return "/admin/login";
    }
  }
  if ((to.path === "/login" || to.path === "/register" || to.path === "/forgot") && auth.token) {
    return "/home";
  }
  return true;
});

export default router;
