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
  if ((to.path === "/login" || to.path === "/register" || to.path === "/forgot") && auth.token) {
    return "/home";
  }
  return true;
});

export default router;
