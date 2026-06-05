<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="brand">
        <div class="brand-title">e-business</div>
        <div class="brand-subtitle">管理后台</div>
      </div>

      <el-menu
        :default-active="activePath"
        class="admin-menu"
        background-color="#111827"
        text-color="#d1d5db"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/admin/dashboard">首页数据看板</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/feedback">用户反馈</el-menu-item>
        <el-menu-item index="/admin/csr-applications">客服申请</el-menu-item>
      </el-menu>
    </aside>

    <section class="admin-content">
      <header class="admin-header">
        <div>
          <h2>管理员后台</h2>
          <p>当前账号：{{ adminLabel }}</p>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </header>

      <main class="admin-main">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { useAuthStore } from "@/store/auth";
import { logout as logoutApi } from "@/api/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activePath = computed(() => route.path);
const adminLabel = computed(() => authStore.user?.email || "admin@local");

const handleLogout = async () => {
  // 先确认（用户取消直接返回）
  try {
    await ElMessageBox.confirm("退出后将返回登录页，未保存的后台操作不会继续保留。", "退出管理员后台", {
      confirmButtonText: "继续退出",
      cancelButtonText: "留在后台",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    // 取消或关闭弹窗
    return;
  }

  // 调用登出 API（失败不阻止本地清理）
  try {
    await logoutApi();
  } catch (e) {
    console.warn("admin logout api failed", e);
  }

  authStore.clearSession();
  router.push("/admin/login");
};
</script>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
}

.admin-sidebar {
  width: 240px;
  background: #111827;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  padding: 22px 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.admin-header h2 {
  margin: 0;
  font-size: 22px;
}

.admin-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.admin-main {
  padding: 24px;
}

@media (max-width: 768px) {
  .admin-shell {
    flex-direction: column;
  }

  .admin-sidebar {
    width: 100% !important;
    min-width: 0 !important;
    flex-shrink: 0;
    position: static;
  }

  .admin-sidebar.collapsed-mobile {
    display: none;
  }

  .admin-main {
    padding: 12px !important;
  }

  .admin-header {
    padding: 12px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .admin-header h2 {
    font-size: 18px;
  }

  .el-table {
    font-size: 12px;
  }

  .admin-card,
  .el-card {
    margin-bottom: 12px;
  }
}
</style>
