<template>
  <div class="profile-page">
    <div class="profile-shell">
      <div class="profile-header">
        <div>
          <h2>鎴戠殑璧勬枡</h2>
          <div>{{ profile.email }}</div>
        </div>
        <el-button type="danger" plain @click="handleLogout">閫€鍑虹櫥褰?/el-button>
      </div>

      <div class="profile-card">
        <h3 class="section-title">鍩虹淇℃伅</h3>
        <el-form :model="profileForm" label-position="top">
          <el-form-item label="鏄电О">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
          <el-form-item label="澶村儚">
            <el-upload
              class="upload-demo"
              :show-file-list="false"
              :http-request="handleUpload"
            >
              <el-button>涓婁紶澶村儚</el-button>
            </el-upload>
            <div v-if="profileForm.avatarUrl" style="margin-top: 12px;">
              <img :src="profileForm.avatarUrl" alt="avatar" style="width: 88px; height: 88px; border-radius: 50%; object-fit: cover;" />
            </div>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="saving" @click="saveProfile">淇濆瓨璧勬枡</el-button>
        </el-form>
      </div>

      <div class="profile-card">
        <h3 class="section-title">鏀惰揣鍦板潃绠＄悊</h3>
        <el-button @click="openNewAddress" style="margin-bottom:8px">鏂板鏀惰揣鍦板潃</el-button>
        <div class="addr-list">
          <div v-for="addr in addresses" :key="addr.id" class="addr-row">
            <div class="addr-main">
              <div class="addr-cols">
                <div class="addr-left">
                  <div class="label">鏀朵欢浜猴細</div>
                  <div class="label">鎵嬫満锛?/div>
                  <div class="label">鍦板潃锛?/div>
                </div>
                <div class="addr-right">
                  <div class="value">{{ addr.name }} <span v-if="addr.isDefault">锛堥粯璁わ級</span></div>
                  <div class="value">{{ addr.phone }}</div>
                  <div class="value">{{ formatAddress(addr) }}</div>
                </div>
              </div>
            </div>
            <div class="addr-actions">
              <el-button size="mini" @click="startEdit(addr)">缂栬緫</el-button>
              <el-button size="mini" type="danger" @click="remove(addr.id)">鍒犻櫎</el-button>
              <el-button v-if="!addr.isDefault" size="mini" @click="setDefault(addr.id)">璁句负榛樿</el-button>
            </div>
          </div>
        </div>
        
      </div>

      <el-dialog title="鏀惰揣鍦板潃" v-model="addrVisible">
        <el-form :model="addrForm" label-position="top">
          <el-form-item label="鏀朵欢浜?>
            <el-input v-model="addrForm.name" />
          </el-form-item>
          <el-form-item label="鎵嬫満">
            <el-input v-model="addrForm.phone" />
          </el-form-item>
          <el-form-item label="鎵€鍦ㄥ湴鍖?>
            <el-cascader
              v-model="addrForm.regionPath"
              :options="regionOptions"
              filterable
              clearable
              placeholder="璇烽€夋嫨鐪?/ 甯?/ 鍖?
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="璇︾粏鍦板潃">
            <el-input v-model="addrForm.detail" placeholder="琛楅亾銆佸皬鍖恒€侀棬鐗屽彿绛? />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="addrForm.isDefault">璁句负榛樿鍦板潃</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addrVisible = false">鍙栨秷</el-button>
          <el-button type="primary" @click="saveAddr">淇濆瓨</el-button>
        </template>
      </el-dialog>

      <el-dialog title="瑁佸壀澶村儚" v-model="cropVisible" width="420px" :close-on-click-modal="false">
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
          <span style="font-size:12px; color:var(--text-secondary); white-space:nowrap;">缂╂斁</span>
          <el-slider v-model="cropScale" :min="0.2" :max="5" :step="0.01" style="flex:1;" />
          <el-button size="mini" @click="resetCrop">閲嶇疆</el-button>
        </div>
        <template #footer>
          <el-button @click="cropVisible = false">鍙栨秷</el-button>
          <el-button type="primary" color="#ff6a3d" :loading="cropping" @click="confirmCrop">纭畾</el-button>
        </template>
      </el-dialog>

      <div class="profile-card">
        <h3 class="section-title">淇敼閭</h3>
        <el-form :model="emailForm" label-position="top">
          <el-form-item label="鏂伴偖绠?>
            <el-input v-model="emailForm.newEmail" />
          </el-form-item>
          <el-form-item label="楠岃瘉鐮?>
            <el-input v-model="emailForm.code" placeholder="6-digit code">
              <template #append>
                <el-button :disabled="emailCountdown > 0" @click="sendEmailCode">
                  {{ emailCountdown > 0 ? `${emailCountdown}s` : "Send" }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="savingEmail" @click="saveEmail">鏇存柊閭</el-button>
        </el-form>
      </div>

      <div class="profile-card">
        <h3 class="section-title">淇敼瀵嗙爜</h3>
        <el-form :model="passwordForm" label-position="top">
          <el-form-item label="鏃у瘑鐮?>
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="鏂板瘑鐮?>
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
            <div class="password-strength" v-if="passwordForm.newPassword">
              <span class="strength-label">瀵嗙爜寮哄害锛?/span>
              <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
              <span class="strength-tip">寤鸿鑷冲皯8浣嶏紝鍖呭惈澶у皬鍐欏瓧姣嶃€佹暟瀛楀拰绗﹀彿</span>
            </div>
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="savingPassword" @click="savePassword">鏇存柊瀵嗙爜</el-button>
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
// 浣跨敤缁熶竴鍦板潃 API锛堟寜璐﹀彿闅旂锛?
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
  if (score <= 1) return { level: "weak", label: "寮? };
  if (score <= 3) return { level: "medium", label: "涓? };
  return { level: "strong", label: "寮? };
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
    ElMessage.warning("璇峰～鍐欏畬鏁村湴鍧€淇℃伅");
    return;
  }
  const regionText = addrForm.regionPath.join(" / ");
  const full = [regionText, addrForm.detail].filter(Boolean).join(" ");
  try {
    if (addrForm.id) {
      await updateAddress(addrForm.id, { ...addrForm, regionText, full });
      ElMessage.success("鍦板潃宸叉洿鏂?);
    } else {
      const res = await createAddress({ ...addrForm, regionText, full });
      // 濡傛灉鍚庣杩斿洖鏂?id锛岃祴鍊间互淇濇寔涓€鑷?
      addrForm.id = res.data?.id ?? addrForm.id;
      ElMessage.success("鍦板潃宸叉柊澧?);
    }
    if (addrForm.isDefault) {
      await setDefaultAddress(addrForm.id);
    }
  } catch (e) {
    console.warn(e);
    ElMessage.error("淇濆瓨鍦板潃澶辫触");
  } finally {
    addrVisible.value = false;
    await loadAddresses();
  }
};

const remove = async (id) => {
  const confirmed = await ElMessageBox.confirm(`纭鍒犻櫎璇ュ湴鍧€锛焋, "鍒犻櫎鍦板潃", {
    confirmButtonText: "鍒犻櫎",
    cancelButtonText: "鍙栨秷",
    type: "warning",
    customClass: "pretty-confirm-box pretty-confirm-box--danger",
    distinguishCancelAndClose: true,
    center: true
  }).then(() => true).catch(() => false);
  if (!confirmed) return;
  try {
    await deleteAddress(id);
    ElMessage.success("鍦板潃宸插垹闄?);
    await loadAddresses();
  } catch (e) {
    console.warn(e);
    ElMessage.error("鍒犻櫎澶辫触");
  }
};

const setDefault = async (id) => {
  try {
    await setDefaultAddress(id);
    ElMessage.success("宸茶涓洪粯璁ゅ湴鍧€");
    await loadAddresses();
  } catch (e) {
    console.warn(e);
    ElMessage.error("璁剧疆榛樿鍦板潃澶辫触");
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
    // Point (px,py) in image 鈫?container: Cx = 160 + px*s + cropX, Cy = 160 + py*s + cropY
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
    ElMessage.success("澶村儚涓婁紶鎴愬姛");
  } catch (e) {
    console.warn(e);
    ElMessage.error("瑁佸壀涓婁紶澶辫触");
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
    ElMessage.success("璧勬枡宸叉洿鏂?);
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
    ElMessage.warning("璇峰厛杈撳叆鏂伴偖绠?);
    return;
  }
  await sendCode(emailForm.newEmail, "CHANGE_EMAIL");
  ElMessage.success("楠岃瘉鐮佸凡鍙戦€?);
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
    ElMessage.success("閭宸叉洿鏂?);
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
    ElMessage.success("瀵嗙爜宸叉洿鏂?);
  } finally {
    savingPassword.value = false;
  }
};

const isAdminEmail = (email) => String(email || "").trim().toLowerCase() === "admin@local";

const handleLogout = async () => {
  const email = authStore.user?.email;
  const admin = isAdminEmail(email);
  // 闈炵鐞嗗憳鏄剧ず纭瀵硅瘽妗嗭紝鍙栨秷鏃剁洿鎺ヨ繑鍥?
  if (!admin) {
    try {
      await ElMessageBox.confirm("閫€鍑哄悗灏嗗洖鍒扮櫥褰曢〉锛屼綘鍙互闅忔椂鍐嶆鐧诲綍銆?, "閫€鍑虹櫥褰?, {
        confirmButtonText: "缁х画閫€鍑?,
        cancelButtonText: "鏆備笉閫€鍑?,
        type: "warning",
        customClass: "pretty-confirm-box",
        distinguishCancelAndClose: true,
        center: true
      });
    } catch (e) {
      // 鐢ㄦ埛鍙栨秷鎴栧叧闂脊绐楋紝浠€涔堥兘涓嶅仛
      return;
    }
  }

  // 鍙戣捣鐧诲嚭璇锋眰锛堝け璐ヤ笉闃绘鏈湴娓呯悊锛?
  try {
    await logout();
  } catch (e) {
    // 蹇界暐鎺ュ彛閿欒
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
/* 纭繚 el-dialog 鍦?profile 椤垫湁鍚堢悊灏哄鍜屽眳涓睍绀?*/
.el-overlay-dialog .el-dialog {
  width: min(760px, 92%) !important;
  margin: 60px auto !important;
  display: block !important;
  box-sizing: border-box !important;
}
.el-overlay-dialog { display: block !important; }
.el-overlay.el-modal-dialog { display: block !important; }

/* 鍦板潃鍒楄〃涓ゅ垪甯冨眬锛氬乏渚ф爣绛撅紙鍙冲榻愶紝鍚啋鍙凤級锛屽彸渚т负瀵瑰簲鍊?*/
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

/* 澶村儚瑁佸壀 */
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
  .profile-shell {
    width: 100% !important;
    max-width: 100% !important;
    padding: 16px !important;
    border-radius: 0 !important;
  }
  .el-input, .el-button {
    width: 100% !important;
  }
  .profile-page {
    padding: 12px !important;
    min-height: auto !important;
  }
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  h1, h2, h3 { font-size: 1.2em !important; }
}
</style>
