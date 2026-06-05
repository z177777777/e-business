<template>
  <div class="profile-page">
    <div class="profile-shell">
      <div class="profile-header">
        <div>
          <h2>我的资料</h2>
          <div>{{ profile.email }}</div>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>

      <div class="profile-card">
        <h3 class="section-title">基础信息</h3>
        <el-form :model="profileForm" label-position="top">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
          <el-form-item label="头像">
            <el-upload
              class="upload-demo"
              :show-file-list="false"
              :http-request="handleUpload"
            >
              <el-button>上传头像</el-button>
            </el-upload>
            <div v-if="profileForm.avatarUrl" style="margin-top: 12px;">
              <img :src="profileForm.avatarUrl" alt="avatar" style="width: 88px; height: 88px; border-radius: 50%; object-fit: cover;" />
            </div>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="saving" @click="saveProfile">保存资料</el-button>
        </el-form>
      </div>

      <div class="profile-card">
        <h3 class="section-title">收货地址管理</h3>
        <el-button @click="openNewAddress" style="margin-bottom:8px">新增收货地址</el-button>
        <div class="addr-list">
          <div v-for="addr in addresses" :key="addr.id" class="addr-row">
            <div class="addr-main">
              <div class="addr-cols">
                <div class="addr-left">
                  <div class="label">收件人：</div>
                  <div class="label">手机：</div>
                  <div class="label">地址：</div>
                </div>
                <div class="addr-right">
                  <div class="value">{{ addr.name }} <span v-if="addr.isDefault">（默认）</span></div>
                  <div class="value">{{ addr.phone }}</div>
                  <div class="value">{{ formatAddress(addr) }}</div>
                </div>
              </div>
            </div>
            <div class="addr-actions">
              <el-button size="mini" @click="startEdit(addr)">编辑</el-button>
              <el-button size="mini" type="danger" @click="remove(addr.id)">删除</el-button>
              <el-button v-if="!addr.isDefault" size="mini" @click="setDefault(addr.id)">设为默认</el-button>
            </div>
          </div>
        </div>
        
      </div>

      <el-dialog title="收货地址" v-model="addrVisible">
        <el-form :model="addrForm" label-position="top">
          <el-form-item label="收件人">
            <el-input v-model="addrForm.name" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="addrForm.phone" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-cascader
              v-model="addrForm.regionPath"
              :options="regionOptions"
              filterable
              clearable
              placeholder="请选择省 / 市 / 区"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input v-model="addrForm.detail" placeholder="街道、小区、门牌号等" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="addrForm.isDefault">设为默认地址</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addrVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddr">保存</el-button>
        </template>
      </el-dialog>

      <el-dialog title="裁剪头像" v-model="cropVisible" width="420px" :close-on-click-modal="false">
        <div class="crop-container" ref="cropContainerRef"
          @mousedown="onCropMouseDown" @mousemove="onCropMouseMove" @mouseup="onCropMouseUp" @mouseleave="onCropMouseUp"
          @touchstart.prevent="onCropTouchStart" @touchmove.prevent="onCropTouchMove" @touchend="onCropMouseUp">
          <img v-if="cropImgSrc" :src="cropImgSrc" class="crop-image" ref="cropImgRef"
            :style="{ transform: `translate(${cropX}px, ${cropY}px) scale(${cropScale})` }"
            @load="onCropImgLoad"
            draggable="false" />
          <div class="crop-overlay"></div>
          <div class="crop-border"></div>
        </div>
        <div style="display:flex; align-items:center; gap:12px; margin-top:16px; padding:0 4px;">
          <span style="font-size:12px; color:var(--text-secondary); white-space:nowrap;">缩放</span>
          <el-slider v-model="cropScale" :min="0.2" :max="5" :step="0.01" style="flex:1;" />
          <el-button size="mini" @click="resetCrop">重置</el-button>
        </div>
        <template #footer>
          <el-button @click="cropVisible = false">取消</el-button>
          <el-button type="primary" color="#ff6a3d" :loading="cropping" @click="confirmCrop">确定</el-button>
        </template>
      </el-dialog>

      <div class="profile-card">
        <h3 class="section-title">修改邮箱</h3>
        <el-form :model="emailForm" label-position="top">
          <el-form-item label="新邮箱">
            <el-input v-model="emailForm.newEmail" />
          </el-form-item>
          <el-form-item label="验证码">
            <el-input v-model="emailForm.code" placeholder="6-digit code">
              <template #append>
                <el-button :disabled="emailCountdown > 0" @click="sendEmailCode">
                  {{ emailCountdown > 0 ? `${emailCountdown}s` : "Send" }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="savingEmail" @click="saveEmail">更新邮箱</el-button>
        </el-form>
      </div>

      <div class="profile-card">
        <h3 class="section-title">修改密码</h3>
        <el-form :model="passwordForm" label-position="top">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
            <div class="password-strength" v-if="passwordForm.newPassword">
              <span class="strength-label">密码强度：</span>
              <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
              <span class="strength-tip">建议至少8位，包含大小写字母、数字和符号</span>
            </div>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="savingPassword" @click="savePassword">更新密码</el-button>
        </el-form>
      </div>
      
      
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { regionOptions } from "@/data/regions";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { logout, sendCode } from "@/api/auth";
import { getProfile, updateEmail, updatePassword, updateProfile } from "@/api/user";
import { uploadImage } from "@/api/files";
// 使用统一地址 API（按账号隔离）
import { useAuthStore } from "@/store/auth";
import { listAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from "@/api/address";

const authStore = useAuthStore();
const router = useRouter();

const profile = reactive({
  email: "",
  nickname: "",
  avatarUrl: ""
});

const profileForm = reactive({
  nickname: "",
  avatarUrl: ""
});

const emailForm = reactive({
  newEmail: "",
  code: ""
});

const passwordForm = reactive({
  oldPassword: "",
  newPassword: ""
});

const calcPasswordStrength = (pwd) => {
  const value = String(pwd || "");
  if (!value) return { level: "none", label: "" };
  let score = 0;
  if (value.length >= 8) score += 1;
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score += 1;
  if (/\d/.test(value)) score += 1;
  if (/[^A-Za-z0-9]/.test(value)) score += 1;
  if (score <= 1) return { level: "weak", label: "弱" };
  if (score <= 3) return { level: "medium", label: "中" };
  return { level: "strong", label: "强" };
};

const passwordStrength = computed(() => calcPasswordStrength(passwordForm.newPassword));

const saving = ref(false);
const savingEmail = ref(false);
const savingPassword = ref(false);
const emailCountdown = ref(0);
let emailTimer = null;

const loadProfile = async () => {
  const res = await getProfile();
  profile.email = res.data.email;
  profile.nickname = res.data.nickname;
  profile.avatarUrl = res.data.avatarUrl || "";
  profileForm.nickname = profile.nickname;
  profileForm.avatarUrl = profile.avatarUrl;
};

const addresses = ref([]);
const addrVisible = ref(false);
const addrForm = reactive({ id: null, name: "", phone: "", regionPath: [], detail: "", full: "", isDefault: false });

const formatAddress = (addr) => {
  const regionText = Array.isArray(addr.regionPath) && addr.regionPath.length ? addr.regionPath.join(" / ") : addr.regionText || "";
  const detailText = addr.detail || addr.full || "";
  return [regionText, detailText].filter(Boolean).join(" ");
};

const loadAddresses = async () => {
  try {
    const res = await listAddresses();
    addresses.value = res.data || [];
  } catch (e) {
    addresses.value = [];
  }
};

const openNewAddress = () => {
  addrForm.id = null;
  addrForm.name = "";
  addrForm.phone = "";
  addrForm.regionPath = [];
  addrForm.detail = "";
  addrForm.full = "";
  addrForm.isDefault = false;
  addrVisible.value = true;
};

const startEdit = (addr) => {
  addrForm.id = addr.id;
  addrForm.name = addr.name;
  addrForm.phone = addr.phone;
  addrForm.regionPath = Array.isArray(addr.regionPath) ? [...addr.regionPath] : [];
  addrForm.detail = addr.detail || (!addr.regionPath ? addr.full || "" : "");
  addrForm.full = addr.full || "";
  addrForm.isDefault = !!addr.isDefault;
  addrVisible.value = true;
};

const saveAddr = async () => {
  if (!addrForm.name || !addrForm.phone || !addrForm.regionPath?.length || !addrForm.detail) {
    ElMessage.warning("请填写完整地址信息");
    return;
  }
  const regionText = addrForm.regionPath.join(" / ");
  const full = [regionText, addrForm.detail].filter(Boolean).join(" ");
  try {
    if (addrForm.id) {
      await updateAddress(addrForm.id, { ...addrForm, regionText, full });
      ElMessage.success("地址已更新");
    } else {
      const res = await createAddress({ ...addrForm, regionText, full });
      // 如果后端返回新 id，赋值以保持一致
      addrForm.id = res.data?.id ?? addrForm.id;
      ElMessage.success("地址已新增");
    }
    if (addrForm.isDefault) {
      await setDefaultAddress(addrForm.id);
    }
  } catch (e) {
    console.warn(e);
    ElMessage.error("保存地址失败");
  } finally {
    addrVisible.value = false;
    await loadAddresses();
  }
};

const remove = async (id) => {
  const confirmed = await ElMessageBox.confirm(`确认删除该地址？`, "删除地址", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
    customClass: "pretty-confirm-box pretty-confirm-box--danger",
    distinguishCancelAndClose: true,
    center: true
  }).then(() => true).catch(() => false);
  if (!confirmed) return;
  try {
    await deleteAddress(id);
    ElMessage.success("地址已删除");
    await loadAddresses();
  } catch (e) {
    console.warn(e);
    ElMessage.error("删除失败");
  }
};

const setDefault = async (id) => {
  try {
    await setDefaultAddress(id);
    ElMessage.success("已设为默认地址");
    await loadAddresses();
  } catch (e) {
    console.warn(e);
    ElMessage.error("设置默认地址失败");
  }
};

const handleUpload = async (options) => {
  const file = options.file;
  const reader = new FileReader();
  reader.onload = (e) => {
    cropImgSrc.value = e.target.result;
    imgW.value = 0;
    imgH.value = 0;
    cropVisible.value = true;
    cropX.value = 0;
    cropY.value = 0;
  };
  reader.readAsDataURL(file);
};

// Crop state
const cropVisible = ref(false);
const cropImgSrc = ref("");
const cropImgRef = ref(null);
const cropX = ref(0);
const cropY = ref(0);
const cropScale = ref(1);
const cropping = ref(false);
const cropContainerRef = ref(null);
const imgW = ref(0);
const imgH = ref(0);

const CONTAINER = 320;
const CROP_SIZE = 280;
const OUT_SIZE = 400;

const onCropImgLoad = () => {
  const img = cropImgRef.value;
  if (!img) return;
  imgW.value = img.naturalWidth;
  imgH.value = img.naturalHeight;
  const fitScale = Math.max(CROP_SIZE / imgW.value, CROP_SIZE / imgH.value);
  cropScale.value = Math.round(fitScale * 100) / 100;
  cropX.value = 0;
  cropY.value = 0;
};

const resetCrop = () => {
  if (imgW.value > 0 && imgH.value > 0) {
    const fitScale = Math.max(CROP_SIZE / imgW.value, CROP_SIZE / imgH.value);
    cropScale.value = Math.round(fitScale * 100) / 100;
  } else {
    cropScale.value = 1;
  }
  cropX.value = 0;
  cropY.value = 0;
};

// Drag state
const isDragging = ref(false);
const dragStartX = ref(0);
const dragStartY = ref(0);

const onCropMouseDown = (e) => {
  isDragging.value = true;
  dragStartX.value = e.clientX - cropX.value;
  dragStartY.value = e.clientY - cropY.value;
};

const onCropMouseMove = (e) => {
  if (!isDragging.value) return;
  cropX.value = e.clientX - dragStartX.value;
  cropY.value = e.clientY - dragStartY.value;
};

const onCropMouseUp = () => {
  isDragging.value = false;
};

const onCropTouchStart = (e) => {
  if (e.touches.length === 1) {
    isDragging.value = true;
    dragStartX.value = e.touches[0].clientX - cropX.value;
    dragStartY.value = e.touches[0].clientY - cropY.value;
  }
};

const onCropTouchMove = (e) => {
  if (!isDragging.value || e.touches.length !== 1) return;
  cropX.value = e.touches[0].clientX - dragStartX.value;
  cropY.value = e.touches[0].clientY - dragStartY.value;
};

const confirmCrop = async () => {
  cropping.value = true;
  try {
    const canvas = document.createElement("canvas");
    canvas.width = CROP_SIZE;
    canvas.height = CROP_SIZE;
    const ctx = canvas.getContext("2d");

    const img = new Image();
    img.crossOrigin = "anonymous";
    img.src = cropImgSrc.value;
    await new Promise((resolve, reject) => {
      img.onload = resolve;
      img.onerror = reject;
    });

    const s = cropScale.value;
    // CSS rendering: image top-left at (160,160), transform: translate(cropX,cropY) scale(s), transform-origin: 0 0
    // Point (px,py) in image → container: Cx = 160 + px*s + cropX, Cy = 160 + py*s + cropY
    // Crop area in container: center (160,160), radius 140
    // Canvas (0,0) maps to Container (160-140, 160-140) = (20,20)
    // canvas_x = Cx - 20 = 160 + px*s + cropX - 20 = px*s + cropX + 140

    ctx.setTransform(s, 0, 0, s, cropX.value + 140, cropY.value + 140);
    ctx.drawImage(img, 0, 0);

    // Scale and clip to circle
    const out = document.createElement("canvas");
    out.width = OUT_SIZE;
    out.height = OUT_SIZE;
    const outCtx = out.getContext("2d");
    outCtx.beginPath();
    outCtx.arc(OUT_SIZE / 2, OUT_SIZE / 2, OUT_SIZE / 2, 0, Math.PI * 2);
    outCtx.clip();
    outCtx.drawImage(canvas, 0, 0, CROP_SIZE, CROP_SIZE, 0, 0, OUT_SIZE, OUT_SIZE);

    const blob = await new Promise((resolve) => out.toBlob(resolve, "image/jpeg", 0.92));
    const croppedFile = new File([blob], "avatar_" + Date.now() + ".jpg", { type: "image/jpeg" });

    const res = await uploadImage(croppedFile);
    profileForm.avatarUrl = res.data;
    cropVisible.value = false;
    ElMessage.success("头像上传成功");
  } catch (e) {
    console.warn(e);
    ElMessage.error("裁剪上传失败");
  } finally {
    cropping.value = false;
  }
};

const saveProfile = async () => {
  saving.value = true;
  try {
    const res = await updateProfile({
      nickname: profileForm.nickname,
      avatarUrl: profileForm.avatarUrl
    });
    authStore.updateUser(res.data);
    await loadProfile();
    ElMessage.success("资料已更新");
  } finally {
    saving.value = false;
  }
};

const startEmailCountdown = () => {
  emailCountdown.value = 60;
  emailTimer = setInterval(() => {
    emailCountdown.value -= 1;
    if (emailCountdown.value <= 0) {
      clearInterval(emailTimer);
    }
  }, 1000);
};

const sendEmailCode = async () => {
  if (!emailForm.newEmail) {
    ElMessage.warning("请先输入新邮箱");
    return;
  }
  await sendCode(emailForm.newEmail, "CHANGE_EMAIL");
  ElMessage.success("验证码已发送");
  startEmailCountdown();
};

const saveEmail = async () => {
  savingEmail.value = true;
  try {
    const res = await updateEmail({
      newEmail: emailForm.newEmail,
      code: emailForm.code
    });
    authStore.updateUser(res.data);
    await loadProfile();
    ElMessage.success("邮箱已更新");
  } finally {
    savingEmail.value = false;
  }
};

const savePassword = async () => {
  savingPassword.value = true;
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    ElMessage.success("密码已更新");
  } finally {
    savingPassword.value = false;
  }
};

const isAdminEmail = (email) => String(email || "").trim().toLowerCase() === "admin@local";

const handleLogout = async () => {
  const email = authStore.user?.email;
  const admin = isAdminEmail(email);
  // 非管理员显示确认对话框，取消时直接返回
  if (!admin) {
    try {
      await ElMessageBox.confirm("退出后将回到登录页，你可以随时再次登录。", "退出登录", {
        confirmButtonText: "继续退出",
        cancelButtonText: "暂不退出",
        type: "warning",
        customClass: "pretty-confirm-box",
        distinguishCancelAndClose: true,
        center: true
      });
    } catch (e) {
      // 用户取消或关闭弹窗，什么都不做
      return;
    }
  }

  // 发起登出请求（失败不阻止本地清理）
  try {
    await logout();
  } catch (e) {
    // 忽略接口错误
    console.warn("logout api failed", e);
  }

  authStore.clearSession();
  router.push(admin ? "/admin/login" : "/login");
};

onMounted(() => {
  loadProfile();
  loadAddresses();
});
</script>

<style scoped>
/* 确保 el-dialog 在 profile 页有合理尺寸和居中展示 */
.el-overlay-dialog .el-dialog {
  width: min(760px, 92%) !important;
  margin: 60px auto !important;
  display: block !important;
  box-sizing: border-box !important;
}
.el-overlay-dialog { display: block !important; }
.el-overlay.el-modal-dialog { display: block !important; }

/* 地址列表两列布局：左侧标签（右对齐，含冒号），右侧为对应值 */
.addr-list { display: block; }
.addr-row { display: flex; justify-content: space-between; align-items: flex-start; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.addr-main { flex: 1; min-width: 0; }
.addr-cols { display: flex; }
.addr-left { width: 96px; padding-right: 8px; text-align: right; color: var(--text-secondary); flex-shrink: 0; }
.addr-left .label { margin: 4px 0; }
.addr-right { flex: 1; padding-left: 8px; }
.addr-right .value { margin: 4px 0; word-break: break-word; }
.addr-actions { display:flex; gap:8px; margin-left: 12px; flex-shrink: 0; }

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.strength-label,
.strength-tip {
  color: var(--text-secondary);
}

.strength-value {
  font-weight: 700;
}

.strength-weak {
  color: #e03131;
}

.strength-medium {
  color: #f08c00;
}

.strength-strong {
  color: #2f9e44;
}

@media (max-width: 600px) {
  .addr-row { flex-direction: column; }
  .addr-cols { flex-direction: row; }
  .addr-left { width: 86px; }
  .addr-actions { margin-top: 8px; }
}

/* 头像裁剪 */
.crop-container {
  width: 320px;
  height: 320px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  border-radius: 4px;
  background: #1a1a1a;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
}

.crop-container:active {
  cursor: grabbing;
}

.crop-image {
  position: absolute;
  left: 50%;
  top: 50%;
  transform-origin: 0 0;
  max-width: none;
  pointer-events: none;
}

.crop-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.55);
}

.crop-border {
  position: absolute;
  inset: 20px;
  border: 2px dashed rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  pointer-events: none;
}

@media (max-width: 768px) {
  .profile-shell { width: 100% !important; padding: 12px !important; border-radius: 0 !important; }
  .profile-page { padding: 8px !important; }
  .el-input, .el-button { width: 100% !important; }
  h1, h2, h3 { font-size: 1.2em !important; }
}
</style>
