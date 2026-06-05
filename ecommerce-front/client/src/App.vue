<template>
  <div :class="['app-shell', { 'no-padding': hideSidebar }]">
    <router-view />
    <aside v-if="!hideSidebar" class="side-toolbar">
      <button v-if="showHomeItem" class="side-item" type="button" @click="goHome">首页</button>
      <button v-if="showProfileItem" class="side-item" type="button" @click="goProfile">我的</button>
      <button class="side-item" type="button" @click="goCart">购物车</button>
      <button class="side-item" type="button" @click="goOrders">订单</button>
      <button class="side-item" type="button" @click="handleAction('service')">客服</button>
      <button class="side-item" type="button" @click="handleFeedback">反馈</button>
      <button v-if="showBackTop" class="side-item" type="button" @click="backToTop">回顶部</button>
    </aside>

    <!-- 反馈侧边栏 -->
    <teleport to="body">
      <div v-if="showFeedbackPanel" class="feedback-overlay" @click.self="showFeedbackPanel = false">
        <div class="feedback-sidebar">
          <div class="feedback-header">
            <h3>意见反馈</h3>
            <button class="feedback-close" type="button" @click="showFeedbackPanel = false">&times;</button>
          </div>
          <div class="feedback-body">
            <template v-if="!auth.token">
              <div class="feedback-login-tip">请先登录后再提交反馈</div>
            </template>
            <template v-else>
              <div class="feedback-field">
                <label>反馈类型</label>
                <el-radio-group v-model="feedbackForm.type">
                  <el-radio value="FEEDBACK">问题反馈</el-radio>
                  <el-radio value="SUGGESTION">功能建议</el-radio>
                  <el-radio value="OTHER">其他</el-radio>
                </el-radio-group>
              </div>
              <div class="feedback-field" v-if="feedbackForm.type === 'OTHER'">
                <label>自定义类型</label>
                <el-input
                  v-model="feedbackForm.customType"
                  placeholder="请输入反馈类型，如：建议、投诉..."
                  maxlength="20"
                />
              </div>
              <div class="feedback-field">
                <label>反馈内容</label>
                <el-input
                  v-model="feedbackForm.content"
                  type="textarea"
                  :rows="6"
                  maxlength="500"
                  show-word-limit
                  placeholder="请详细描述你的问题或建议...（至少五个字）"
                />
              </div>
              <el-button
                type="primary"
                :loading="submitting"
                @click="handleSubmitFeedback"
                style="width:100%"
              >
                提交反馈
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/store/auth";
import { submitFeedback } from "@/api/feedback";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const showBackTop = ref(false);

const showFeedbackPanel = ref(false);
const feedbackForm = ref({ type: "FEEDBACK", customType: "", content: "" });
const submitting = ref(false);

const isCsr = computed(() => auth.user?.role === "CSR");
const authRoutes = ["/login", "/register", "/forgot"];
const isMainPage = computed(() => !authRoutes.includes(route.path));
const hideSidebar = computed(() => route.path.startsWith('/admin') || route.path.startsWith('/profile') || route.path.startsWith('/chat') || authRoutes.includes(route.path) || isCsr.value);
const showProfileItem = computed(() => !hideSidebar.value && !!auth.token);
const showHomeItem = computed(() => !hideSidebar.value && route.path !== "/home");

const updateBackTop = () => {
  showBackTop.value = window.scrollY > 240;
};

const goHome = () => {
  router.push("/home");
};

const goCart = () => {
  router.push("/cart");
};

const goOrders = () => {
  router.push("/orders");
};

const goProfile = () => {
  router.push("/profile");
};

const backToTop = () => {
  window.scrollTo({ top: 0, behavior: "smooth" });
};

const handleAction = (type) => {
  if (type === "service") {
    if (!auth.token) {
      ElMessage.warning("请先登录后再使用客服功能");
      router.push("/login").catch(() => {});
      return;
    }
    router.push("/chat").catch(() => {});
    return;
  }
  ElMessage.info("功能开发中");
};

const handleFeedback = () => {
  if (!auth.token) {
    ElMessage.warning("请先登录后再提交反馈");
    return;
  }
  feedbackForm.value = { type: "FEEDBACK", customType: "", content: "" };
  showFeedbackPanel.value = true;
};

const handleSubmitFeedback = async () => {
  let type = feedbackForm.value.type;
  if (type === "OTHER") {
    const custom = feedbackForm.value.customType.trim();
    if (!custom) {
      ElMessage.warning("请输入自定义反馈类型");
      return;
    }
    type = custom;
  }
  if (!feedbackForm.value.content.trim()) {
    ElMessage.warning("请输入反馈内容");
    return;
  }
  if (feedbackForm.value.content.trim().length < 5) {
    ElMessage.warning("反馈内容至少5个字");
    return;
  }
  submitting.value = true;
  try {
    await submitFeedback({
      type,
      content: feedbackForm.value.content.trim()
    });
    ElMessage.success("反馈已提交，感谢你的意见！");
    showFeedbackPanel.value = false;
  } catch (e) {
    console.warn("submit feedback failed", e);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  updateBackTop();
  window.addEventListener("scroll", updateBackTop, { passive: true });
});

onBeforeUnmount(() => {
  window.removeEventListener("scroll", updateBackTop);
});
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  padding-right: 96px;
}

.app-shell.no-padding {
  padding-right: 0;
}

.side-toolbar {
  position: fixed;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 0;
  z-index: 50;
  padding: 6px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  border: 0.5px solid rgba(255, 255, 255, 0.35);
  box-shadow: var(--shadow-soft);
}

.side-item {
  width: 78px;
  padding: 10px 8px;
  border: none;
  border-radius: 0;
  background: transparent;
  color: var(--text-primary);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.side-item:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 反馈侧边栏 */
.feedback-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  justify-content: flex-end;
}

.feedback-sidebar {
  width: 380px;
  max-width: 90vw;
  height: 100%;
  background: #fff;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.28s ease;
}

.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.feedback-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.feedback-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.feedback-close:hover {
  background: #f3f4f6;
}

.feedback-body {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.feedback-login-tip {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
  font-size: 14px;
}

.feedback-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feedback-field label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

@media (max-width: 768px) {
  .app-shell {
    padding-right: 0 !important;
  }

  .side-toolbar {
    right: 0;
    top: auto;
    bottom: 0;
    transform: none;
    flex-direction: row;
    width: 100%;
    justify-content: space-around;
    padding: 6px 4px;
    border-radius: 14px 14px 0 0;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(14px);
    box-shadow: 0 -2px 12px rgba(0,0,0,0.08);
  }
  .side-item {
    width: auto;
    flex: 1;
    padding: 8px 4px;
    font-size: 11px;
    border-radius: 8px;
  }
  .feedback-sidebar {
    width: 100%;
    max-width: 100vw;
  }
}
</style>
