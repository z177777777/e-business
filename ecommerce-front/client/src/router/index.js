import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/store/auth";

const isAdminUser = (user) => String(user?.email || "").trim().toLowerCase() === "admin@local";
const isCsrUser = (user) => String(user?.role || "").toUpperCase() === "CSR";

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
    component: () => import("@/views/user/UserLayout.vue"),
    meta: { requiresAuth: true },
    redirect: "/profile/info",
    children: [
      { path: "info", component: () => import("@/views/user/UserInfo.vue"), meta: { requiresAuth: true } },
      { path: "security", component: () => import("@/views/user/UserSecurity.vue"), meta: { requiresAuth: true } },
      { path: "address", component: () => import("@/views/user/UserAddress.vue"), meta: { requiresAuth: true } },
      { path: "wallet", component: () => import("@/views/user/UserWallet.vue"), meta: { requiresAuth: true } },
      { path: "favorites", component: () => import("@/views/user/UserFavorites.vue"), meta: { requiresAuth: true } },
      { path: "service", component: () => import("@/views/user/UserService.vue"), meta: { requiresAuth: true } }
    ]
  }
  ,{ path: "/checkout", component: () => import("@/views/Checkout.vue"), meta: { requiresAuth: true } }
  ,{
    path: "/chat",
    component: () => import("@/views/Chat.vue"),
    meta: { requiresAuth: true }
  }
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
        { path: "orders", component: () => import("@/views/admin/AdminOrders.vue") },
        { path: "feedback", component: () => import("@/views/admin/AdminFeedback.vue") },
        { path: "csr-applications", component: () => import("@/views/admin/AdminCsrApplications.vue") },
        { path: "orders/:id", component: () => import("@/views/OrdersDetail.vue") }
      ]
    }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  const adminUser = isAdminUser(auth.user);
  const csrUser = isCsrUser(auth.user);
  if (to.meta.requiresAuth && !auth.token) {
    return "/login";
  }
  if (to.meta.requiresAdmin) {
    if (!auth.token) {
      return "/admin/login";
    }
    if (!adminUser) {
      return csrUser ? "/chat" : "/home";
    }
  }
  if (to.path === "/admin/login" && auth.token) {
    return adminUser ? "/admin/dashboard" : csrUser ? "/chat" : "/home";
  }
  if ((to.path === "/login" || to.path === "/register" || to.path === "/forgot") && auth.token) {
    if (adminUser) return "/admin/dashboard";
    if (csrUser) return "/chat";
    return "/home";
  }
  // CSR users can only access /chat, /login, /register, /forgot
  if (csrUser && auth.token) {
    const allowed = ["/chat", "/login", "/register", "/forgot"];
    if (!allowed.includes(to.path)) {
      return "/chat";
    }
  }
  return true;
});

export default router;
