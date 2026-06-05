<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar" :class="{ 'hide-mobile': isCsr && activeSession || !isCsr && activeSession }">
      <div class="sidebar-header">
        <div class="header-left">
          <button class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <span class="header-title">{{ isCsr ? '用户会话' : '客服聊天' }}</span>
        </div>
        <button v-if="isCsr" class="logout-btn" type="button" @click="handleLogout">退出</button>
      </div>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.userId"
          :class="['session-item', { active: activeSession === s.userId }]"
          @click="selectSession(s)"
        >
          <div class="session-avatar">
            <img v-if="s.avatarUrl" :src="s.avatarUrl" class="avatar-img" />
            <span v-else>{{ (s.nickname || '?')[0] }}</span>
          </div>
          <div class="session-info">
            <div class="session-name">
              {{ s.nickname }}
              <span v-if="s.recommended && !isCsr" class="session-recommend">推荐</span>
            </div>
            <div class="session-email">{{ s.email }}</div>
            <div class="session-load" v-if="!isCsr">
              <span v-if="s.activeSessions === 0" class="load-idle">空闲</span>
              <span v-else class="load-busy">服务中 {{ s.activeSessions }}人</span>
            </div>
          </div>
        </div>
        <div v-if="sessions.length === 0" class="empty-sessions">
          <p>暂无会话</p>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区 -->
    <div class="chat-main" v-if="activeSession" :class="{ 'show-mobile': activeSession }">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-header-info">
          <button class="mobile-back-btn" @click="activeSession = null">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <div class="chat-avatar">
            <img v-if="activeSessionUser?.avatarUrl" :src="activeSessionUser.avatarUrl" class="avatar-img" />
            <span v-else>{{ (activeSessionUser?.nickname || '?')[0] }}</span>
          </div>
          <div>
            <div class="chat-name">{{ activeSessionUser?.nickname }}</div>
            <div class="chat-status">在线</div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-area" ref="messageAreaRef" @scroll="handleScroll">
        <div v-if="loadingHistory" class="loading-more">加载中...</div>
        <div v-if="!hasMore && messages.length > 0" class="no-more">没有更多消息了</div>
        <div v-for="msg in messages" :key="msg.id" :class="['msg-row', msg.senderId === userId ? 'msg-self' : 'msg-other']">
          <div class="msg-avatar" :class="msg.senderId === userId ? 'avatar-right' : 'avatar-left'">
            <img v-if="msg.senderId === userId ? selfAvatarUrl : otherAvatarUrl" :src="msg.senderId === userId ? selfAvatarUrl : otherAvatarUrl" class="avatar-img" />
            <span v-else class="avatar-char">{{ msg.senderId === userId ? selfAvatarChar : otherAvatarChar }}</span>
          </div>
          <div class="msg-content" :class="msg.senderId === userId ? 'content-right' : 'content-left'">
          <div class="msg-bubble" :class="{ 'msg-self-bubble': msg.senderId === userId }">
            <!-- 文本消息 -->
            <div v-if="msg.messageType === 'TEXT'" class="msg-text">{{ msg.content }}</div>
            <!-- 图片消息 -->
            <div v-else-if="msg.messageType === 'IMAGE'" class="msg-image" @click="previewImage(msg.content)">
              <img :src="msg.content" alt="图片" />
            </div>
            <!-- 视频消息 -->
            <div v-else-if="msg.messageType === 'VIDEO'" class="msg-video" @click="previewVideo(msg.content)">
              <div class="video-play-icon">&#9654;</div>
            </div>
            <!-- 订单卡片消息 -->
            <div v-else-if="msg.messageType === 'ORDER_CARD'" class="msg-order">
              <div class="order-card-title">订单 #{{ msg.orderId }}</div>
              <div class="order-card-content">{{ msg.content }}</div>
              <el-button size="small" type="primary" plain @click="viewOrder(msg.orderId)">查看订单</el-button>
            </div>
            <!-- 默认文本 -->
            <div v-else class="msg-text">{{ msg.content }}</div>
          </div>
          <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
        <div ref="scrollAnchorRef"></div>
      </div>

      <!-- 底部发送区 -->
      <div class="input-area">
        <div class="input-toolbar">
          <label class="tool-btn" title="发送图片">
            <el-icon><Picture /></el-icon>
            <input type="file" accept="image/*" style="display:none" @change="handleFileSelect($event, 'IMAGE')" />
          </label>
          <label class="tool-btn" title="发送视频">
            <el-icon><VideoCamera /></el-icon>
            <input type="file" accept="video/*" style="display:none" @change="handleFileSelect($event, 'VIDEO')" />
          </label>
          <button class="tool-btn" title="发送订单" @click="showOrderPicker = true">
            <el-icon><Tickets /></el-icon>
          </button>
        </div>
        <div class="input-row">
          <textarea
            v-model="inputText"
            class="msg-input"
            placeholder="输入消息..."
            rows="3"
            @keydown.enter.exact.prevent="sendText"
          ></textarea>
          <el-button type="primary" @click="sendText" :disabled="!inputText.trim()">发送</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="chat-empty" v-else>
      <div class="empty-icon">💬</div>
      <p>选择一个会话开始聊天</p>
    </div>

    <!-- 图片预览 -->
    <teleport to="body">
      <div v-if="previewImageUrl" class="img-preview-overlay" @click="previewImageUrl = null">
        <img :src="previewImageUrl" class="img-preview-content" />
      </div>
    </teleport>

    <!-- 视频预览 -->
    <teleport to="body">
      <div v-if="previewVideoUrl" class="img-preview-overlay" @click="previewVideoUrl = null">
        <video :src="previewVideoUrl" class="img-preview-content" controls autoplay></video>
      </div>
    </teleport>

    <!-- 订单选择器 -->
    <teleport to="body">
      <div v-if="showOrderPicker" class="img-preview-overlay" @click.self="showOrderPicker = false">
        <div class="order-picker-panel">
          <h3>选择要发送的订单</h3>
          <div class="order-picker-list">
            <div v-for="order in userOrders" :key="order.id" class="order-picker-item" @click="sendOrderCard(order)">
              <div class="order-item-header">
                <span class="order-item-no">{{ order.orderNo }}</span>
                <el-tag size="small" :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
                <span class="order-item-date">{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="order-items-list">
                <div v-for="item in order.items" :key="item.id" class="order-item-row">
                  <span class="order-item-name">{{ item.productName }}</span>
                  <span class="order-item-qty">x{{ item.quantity }}</span>
                  <span class="order-item-price">¥{{ item.productPrice }}</span>
                </div>
              </div>
              <div class="order-item-footer">
                <span class="order-item-total">合计 ¥{{ order.totalAmount }}</span>
                <span v-if="order.userName" class="order-item-user">{{ order.userName }}</span>
              </div>
            </div>
            <div v-if="userOrders.length === 0" class="empty-orders">暂无订单</div>
          </div>
          <el-button style="width:100%;margin-top:12px" @click="showOrderPicker = false">取消</el-button>
        </div>
      </div>
    </teleport>

    <!-- 订单详情弹窗 -->
    <teleport to="body">
      <div v-if="showOrderDetail" class="img-preview-overlay" @click.self="showOrderDetail = false">
        <div class="order-detail-panel">
          <div class="order-detail-header">
            <h3>订单详情</h3>
            <button class="feedback-close" type="button" @click="showOrderDetail = false">&times;</button>
          </div>
          <div class="order-detail-body" v-if="loadingOrderDetail">
            <div class="loading-more">加载中...</div>
          </div>
          <div class="order-detail-body" v-else-if="orderDetail">
            <div class="detail-section">
              <div class="detail-row">
                <span class="detail-label">订单编号</span>
                <span class="detail-value">{{ orderDetail.orderNo }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">状态</span>
                <el-tag size="small" :type="statusType(orderDetail.status)">{{ statusText(orderDetail.status) }}</el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">下单时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.createdAt) }}</span>
              </div>
              <div v-if="orderDetail.userName" class="detail-row">
                <span class="detail-label">用户</span>
                <span class="detail-value">{{ orderDetail.userName }}</span>
              </div>
            </div>
            <div class="detail-section" v-if="orderDetail.items && orderDetail.items.length">
              <h4 class="section-title">商品明细</h4>
              <div v-for="item in orderDetail.items" :key="item.id" class="detail-item-row">
                <span class="detail-item-name">{{ item.productName }}</span>
                <span class="detail-item-price">¥{{ item.productPrice }} x {{ item.quantity }}</span>
              </div>
              <div class="detail-total">合计：¥{{ orderDetail.totalAmount }}</div>
            </div>
            <div class="detail-section">
              <h4 class="section-title">时间线</h4>
              <div class="detail-row">
                <span class="detail-label">下单时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.createdAt) }}</span>
              </div>
              <div class="detail-row" v-if="orderDetail.paidAt">
                <span class="detail-label">付款时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.paidAt) }}</span>
              </div>
              <div class="detail-row" v-if="orderDetail.shippedAt">
                <span class="detail-label">发货时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.shippedAt) }}</span>
              </div>
              <div class="detail-row" v-if="orderDetail.receivedAt">
                <span class="detail-label">收货时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.receivedAt) }}</span>
              </div>
              <div class="detail-row" v-if="statusText(orderDetail.status) === '已取消' && orderDetail.updatedAt">
                <span class="detail-label">取消时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.updatedAt) }}</span>
              </div>
              <div class="detail-row" v-if="['已退款','退款中'].includes(statusText(orderDetail.status)) && orderDetail.updatedAt">
                <span class="detail-label">退款时间</span>
                <span class="detail-value">{{ formatDate(orderDetail.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { ArrowLeft, Picture, VideoCamera, Tickets } from "@element-plus/icons-vue";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "@/store/auth";
import { getSessions, getMessages, uploadChatFile } from "@/api/chat";
import http from "@/api/http";

const router = useRouter();
const auth = useAuthStore();

const userId = computed(() => auth.user?.id);
const isCsr = computed(() => auth.user?.role === "CSR");
const selfAvatarUrl = computed(() => auth.user?.avatarUrl || "");
const selfAvatarChar = computed(() => (auth.user?.nickname || auth.user?.email || "?")[0]);
const otherAvatarUrl = computed(() => activeSessionUser.value?.avatarUrl || "");
const otherAvatarChar = computed(() => (activeSessionUser.value?.nickname || activeSessionUser.value?.email || "客")[0]);

const sessions = ref([]);
const activeSession = ref(null);
const activeSessionUser = ref(null);
const messages = ref([]);
const inputText = ref("");
const messageAreaRef = ref(null);
const scrollAnchorRef = ref(null);
const loadingHistory = ref(false);
const hasMore = ref(true);
const previewImageUrl = ref(null);
const previewVideoUrl = ref(null);
const showOrderPicker = ref(false);
const userOrders = ref([]);
const pageSize = 30;

let stompClient = null;
let subscription = null;

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return "";
  const d = new Date(timeStr);
  const now = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  const hhmm = pad(d.getHours()) + ":" + pad(d.getMinutes());
  if (d.toDateString() === now.toDateString()) return hhmm;
  return (d.getMonth() + 1) + "/" + d.getDate() + " " + hhmm;
};

// 订单状态映射
const statusMap = {
  PENDING: "待付款",
  PAID: "已付款",
  SHIPPED: "已发货",
  RECEIVED: "已收货",
  CANCELLED: "已取消",
  REFUND_REQUESTED: "退款中",
  REFUNDED: "已退款"
};
const statusText = (s) => statusMap[s] || s;
const statusType = (s) => {
  if (s === "PAID" || s === "RECEIVED" || s === "SHIPPED") return "success";
  if (s === "PENDING") return "warning";
  if (s === "CANCELLED" || s === "REFUNDED") return "info";
  if (s === "REFUND_REQUESTED") return "danger";
  return "";
};
const formatDate = (timeStr) => {
  if (!timeStr) return "";
  const d = new Date(timeStr);
  return (d.getMonth() + 1) + "/" + d.getDate() + " " + d.getHours().toString().padStart(2, "0") + ":" + d.getMinutes().toString().padStart(2, "0");
};

// 加载会话列表
const loadSessions = async () => {
  try {
    const res = await getSessions();
    sessions.value = res.data || [];
    if (sessions.value.length > 0 && !activeSession.value) {
      const recommended = sessions.value.find(s => s.recommended) || sessions.value[0];
      selectSession(recommended);
    }
  } catch (e) {
    console.warn("load sessions failed", e);
  }
};

// 选择会话
const selectSession = (session) => {
  activeSession.value = session.userId;
  activeSessionUser.value = session;
  messages.value = [];
  hasMore.value = true;
  loadHistory();
  if (isCsr.value) {
    loadUserOrders(session.userId);
  }
};

// 加载历史消息
const loadHistory = async () => {
  if (!activeSession.value || loadingHistory.value) return;
  loadingHistory.value = true;
  try {
    let before = null;
    if (messages.value.length > 0) {
      before = messages.value[0].createdAt;
    }
    const res = await getMessages(activeSession.value, before, pageSize);
    const newMsgs = res.data || [];
    if (newMsgs.length < pageSize) {
      hasMore.value = false;
    }
    messages.value = [...newMsgs, ...messages.value];
    if (!before) {
      nextTick(() => scrollToBottom());
    }
  } catch (e) {
    console.warn("load history failed", e);
  } finally {
    loadingHistory.value = false;
  }
};

// 滚动加载更多
const handleScroll = () => {
  const el = messageAreaRef.value;
  if (!el || loadingHistory.value || !hasMore.value) return;
  if (el.scrollTop <= 30) {
    const prevHeight = el.scrollHeight;
    loadHistory().then(() => {
      nextTick(() => {
        el.scrollTop = el.scrollHeight - prevHeight;
      });
    });
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const el = messageAreaRef.value;
    if (el) {
      el.scrollTop = el.scrollHeight;
      // 图片加载后布局高度可能变化，再次滚动
      setTimeout(() => {
        if (el) el.scrollTop = el.scrollHeight;
      }, 150);
    }
  });
};

// 将会话移到列表顶部
const moveSessionToTop = (targetUserId) => {
  const idx = sessions.value.findIndex(s => s.userId === targetUserId);
  if (idx > 0) {
    const session = sessions.value.splice(idx, 1)[0];
    sessions.value.unshift(session);
  }
};

// 本地追加自己的消息并滚动到底部
const addOwnMessage = (content, messageType, orderId) => {
  messages.value.push({
    id: Date.now(),
    senderId: userId.value,
    receiverId: activeSession.value,
    content,
    messageType,
    orderId: orderId || null,
    createdAt: new Date().toISOString()
  });
  nextTick(() => scrollToBottom());
};

// 发送文字消息
const sendText = () => {
  const text = inputText.value.trim();
  if (!text || !stompClient || !stompClient.connected) return;
  stompClient.publish({
    destination: "/app/chat.send",
    body: JSON.stringify({
      senderId: userId.value,
      receiverId: activeSession.value,
      content: text,
      messageType: "TEXT"
    })
  });
  addOwnMessage(text, "TEXT");
  inputText.value = "";
  if (isCsr.value) moveSessionToTop(activeSession.value);
};

// 处理文件选择
const handleFileSelect = async (event, type) => {
  const file = event.target.files[0];
  if (!file) return;
  try {
    const res = await uploadChatFile(file);
    const url = res.data?.url || res.data;
    if (stompClient && stompClient.connected) {
      stompClient.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({
          senderId: userId.value,
          receiverId: activeSession.value,
          content: url,
          messageType: type
        })
      });
      addOwnMessage(url, type);
      if (isCsr.value) moveSessionToTop(activeSession.value);
    }
  } catch (e) {
    ElMessage.error("文件上传失败");
  }
  event.target.value = "";
};

// 发送订单卡片
const sendOrderCard = (order) => {
  const content = `订单 ${order.orderNo}，金额 ¥${order.totalAmount}`;
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        senderId: userId.value,
        receiverId: activeSession.value,
        content: content,
        messageType: "ORDER_CARD",
        orderId: order.id
      })
    });
    addOwnMessage(content, "ORDER_CARD", order.id);
    if (isCsr.value) moveSessionToTop(activeSession.value);
  }
  showOrderPicker.value = false;
};

// 加载用户订单（CSR 加载客户的订单，普通用户加载自己的）
const loadUserOrders = async (targetUserId) => {
  try {
    const url = targetUserId ? `/api/orders?userId=${targetUserId}` : "/api/orders";
    const res = await http.get(url);
    userOrders.value = res.data || [];
  } catch (e) {
    console.warn("load orders failed", e);
    userOrders.value = [];
  }
};

// 图片预览
const previewImage = (url) => {
  previewImageUrl.value = url;
};

// 视频预览
const previewVideo = (url) => {
  previewVideoUrl.value = url;
};

// 订单详情弹窗
const showOrderDetail = ref(false);
const orderDetail = ref(null);
const loadingOrderDetail = ref(false);

// 查看订单
const viewOrder = async (orderId) => {
  if (!orderId) return;
  loadingOrderDetail.value = true;
  showOrderDetail.value = true;
  orderDetail.value = null;
  try {
    const res = await http.get(`/api/orders/${orderId}`);
    orderDetail.value = res.data || res;
  } catch (e) {
    console.warn("load order detail failed", e);
    ElMessage.error("加载订单详情失败");
    showOrderDetail.value = false;
  } finally {
    loadingOrderDetail.value = false;
  }
};

// 返回
const goBack = () => {
  router.back();
};

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm("退出后将回到登录页，你可以随时再次登录。", "退出登录", {
    confirmButtonText: "继续退出",
    cancelButtonText: "暂不退出",
    type: "warning",
    customClass: "pretty-confirm-box",
    distinguishCancelAndClose: true,
    center: true
  }).then(() => {
    disconnectWs();
    auth.clearSession();
    router.push("/login");
  }).catch(() => {});
};

// 连接 WebSocket
const connectWs = () => {
  if (!auth.token) return;
  try {
    const socket = new SockJS("/ws");
    stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: "Bearer " + auth.token
      },
      debug: (str) => { /* console.log(str) */ },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    stompClient.onConnect = () => {
      subscription = stompClient.subscribe("/user/queue/chat", (msg) => {
        try {
          const body = JSON.parse(msg.body);
          // CSR 端：新消息到达时，把该用户会话移到列表顶部
          if (isCsr.value && body.senderId !== userId.value) {
            moveSessionToTop(body.senderId);
            if (!sessions.value.find(s => s.userId === body.senderId)) {
              loadSessions();
            }
          }
          if (body.senderId === activeSession.value || body.receiverId === activeSession.value) {
            messages.value.push(body);
            scrollToBottom();
          }
        } catch (e) {
          // ignore parse errors
        }
      });
    };

    stompClient.onStompError = (frame) => {
      console.warn("STOMP error", frame.headers["message"]);
    };

    stompClient.activate();
  } catch (e) {
    console.warn("WebSocket connection failed", e);
  }
};

// 断开 WebSocket
const disconnectWs = () => {
  if (subscription) {
    subscription.unsubscribe();
    subscription = null;
  }
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
};

onMounted(() => {
  loadSessions();
  if (!isCsr.value) {
    loadUserOrders();
  }
  connectWs();
});

onBeforeUnmount(() => {
  disconnectWs();
});
</script>

<style scoped>
.chat-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  background: #f0f0f0;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 侧边栏 */
.chat-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #333;
}

.logout-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px 10px;
  font-size: 13px;
  color: #999;
  border-radius: 4px;
  transition: background 0.15s, color 0.15s;
}

.logout-btn:hover {
  background: #f5f5f5;
  color: #e74c3c;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #191919;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.session-item:hover {
  background: #f5f5f5;
}

.session-item.active {
  background: #e8f0fe;
}

.session-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #07c160;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 15px;
  color: #191919;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-email {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-load {
  font-size: 11px;
  margin-top: 2px;
}

.load-idle {
  color: #07c160;
}

.load-busy {
  color: #ff6b6b;
}

.session-recommend {
  font-size: 10px;
  color: #fff;
  background: #07c160;
  padding: 1px 6px;
  border-radius: 10px;
  margin-left: 6px;
  flex-shrink: 0;
  vertical-align: middle;
}

.empty-sessions {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

/* 主聊天区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #07c160;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  flex-shrink: 0;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #191919;
}

.chat-status {
  font-size: 12px;
  color: #07c160;
  margin-top: 2px;
}

/* 移动端返回按钮，默认隐藏 */
.mobile-back-btn {
  display: none;
}

/* 消息区域 */
.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #ededed;
  display: flex;
  flex-direction: column;
}

.loading-more,
.no-more {
  text-align: center;
  padding: 10px;
  font-size: 12px;
  color: #999;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 16px;
  max-width: 75%;
}

.msg-self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-other {
  align-self: flex-start;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-char {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.avatar-left {
  background: #07c160;
}

.avatar-right {
  background: #409eff;
}

.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-content {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}

.content-right {
  align-items: flex-end;
}

.content-left {
  align-items: flex-start;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: #fff;
  color: #333;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  max-width: 100%;
}

.msg-self-bubble {
  background: #95ec69;
  color: #000;
}

.msg-text {
  white-space: pre-wrap;
}

.msg-image img {
  max-width: 240px;
  max-height: 240px;
  border-radius: 4px;
  cursor: pointer;
}

.msg-video {
  width: 200px;
  height: 120px;
  background: #333;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.video-play-icon {
  font-size: 36px;
  color: #fff;
  opacity: 0.8;
}

.msg-order {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
}

.order-card-title {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.order-card-content {
  font-size: 12px;
  color: #666;
}

.msg-time {
  font-size: 11px;
  color: #b2b2b2;
  margin-top: 4px;
}

/* 输入区 */
.input-area {
  background: #fff;
  border-top: 1px solid #e0e0e0;
  padding: 0 16px 12px;
}

.input-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.tool-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  color: #666;
  font-size: 18px;
  transition: background 0.15s;
}

.tool-btn:hover {
  background: #f0f0f0;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.msg-input {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  font-family: inherit;
  padding: 6px 0;
}

/* 空状态 */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ededed;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 图片/视频预览 */
.img-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.img-preview-content {
  max-width: 90vw;
  max-height: 90vh;
  border-radius: 8px;
}

/* 订单选择器 */
.order-picker-panel {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 480px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
}

.order-picker-panel h3 {
  margin: 0 0 16px;
  font-size: 16px;
}

.order-picker-list {
  max-height: 50vh;
  overflow-y: auto;
}

.order-picker-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 14px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.order-picker-item:hover {
  background: #f5f5f5;
}

.order-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-item-no {
  font-weight: 600;
  font-size: 14px;
  color: #191919;
}

.order-item-date {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.order-items-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 4px;
  border-left: 2px solid #e8f0fe;
}

.order-item-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.order-item-name {
  flex: 1;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-item-qty {
  color: #999;
  font-size: 12px;
}

.order-item-price {
  color: #e74c3c;
  font-weight: 500;
  font-size: 13px;
  min-width: 60px;
  text-align: right;
}

.order-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-item-total {
  font-weight: 600;
  font-size: 14px;
  color: #e74c3c;
}

.order-item-user {
  font-size: 12px;
  color: #999;
}

.empty-orders {
  text-align: center;
  color: #999;
  padding: 24px;
}

/* 订单详情弹窗 */
.order-detail-panel {
  background: #fff;
  border-radius: 12px;
  width: 520px;
  max-width: 90vw;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.order-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.order-detail-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

.order-detail-body {
  padding: 24px;
  overflow-y: auto;
  max-height: 65vh;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-label {
  width: 80px;
  color: #999;
  flex-shrink: 0;
}

.detail-value {
  color: #191919;
}

.detail-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
}

.detail-item-name {
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-item-price {
  color: #666;
  font-size: 13px;
  flex-shrink: 0;
  margin-left: 12px;
}

.detail-total {
  text-align: right;
  padding-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #e74c3c;
}


/* 移动端响应式 */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 100% !important;
    border-right: none !important;
  }
  .chat-main {
    display: none;
  }
  .chat-main.show-mobile {
    display: flex !important;
  }
  .chat-sidebar.hide-mobile {
    display: none !important;
  }
  .chat-empty {
    display: none;
  }
  .chat-page {
    flex-direction: column;
  }
  .sidebar-header { padding: 12px; }
  .header-title { font-size: 16px; }
  .session-item { padding: 12px; }
  .msg-row { max-width: 90% !important; }
  .msg-avatar { width: 30px; height: 30px; }
  .msg-bubble { font-size: 14px; padding: 8px 12px; }
  .input-area { padding: 0 10px 8px; }
  .msg-input { font-size: 14px; }
  .order-picker-panel { width: 95vw; padding: 16px; }
  .order-detail-panel { width: 95vw; }
  .order-detail-body { max-height: 50vh; padding: 16px; }
  .chat-header { padding: 10px 14px; }
  .chat-avatar { width: 36px; height: 36px; }
  .message-area { padding: 12px; }
  .img-preview-content { max-width: 95vw; max-height: 80vh; }
  .order-picker-list { max-height: 40vh; }
  .session-list { padding: 0; }
  /* 移动端返回按钮 */
  .mobile-back-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: transparent;
    cursor: pointer;
    padding: 4px;
    color: #333;
    flex-shrink: 0;
  }
}

</style>
