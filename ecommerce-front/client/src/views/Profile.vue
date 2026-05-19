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
          </el-form-item>
          <el-button type="primary" color="#ff6a3d" :loading="savingPassword" @click="savePassword">更新密码</el-button>
        </el-form>
      </div>
      
      
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { regionOptions } from "@/data/regions";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { logout, sendCode } from "@/api/auth";
import { getProfile, updateEmail, updatePassword, updateProfile } from "@/api/user";
import { uploadImage } from "@/api/files";
// 使用 localStorage 存储地址（不调用后端）
import { useAuthStore } from "@/store/auth";

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
    const raw = localStorage.getItem('addresses');
    addresses.value = raw ? JSON.parse(raw) : [];
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
  // localStorage 模拟 CRUD
  if (addrForm.id) {
    const idx = addresses.value.findIndex(a => a.id === addrForm.id);
    if (idx !== -1) {
      addresses.value[idx] = {
        ...addresses.value[idx],
        name: addrForm.name,
        phone: addrForm.phone,
        regionPath: [...addrForm.regionPath],
        regionText,
        detail: addrForm.detail,
        full,
        isDefault: addrForm.isDefault
      };
    }
  } else {
    const id = Date.now();
    addresses.value.push({
      id,
      name: addrForm.name,
      phone: addrForm.phone,
      regionPath: [...addrForm.regionPath],
      regionText,
      detail: addrForm.detail,
      full,
      isDefault: addrForm.isDefault
    });
    addrForm.id = id;
  }
  if (addrForm.isDefault) {
    addresses.value.forEach(a => { a.isDefault = (a.id === addrForm.id); });
  }
  try { localStorage.setItem('addresses', JSON.stringify(addresses.value)); } catch (e) { console.warn(e); }
  addrVisible.value = false;
  await loadAddresses();
  ElMessage.success("地址已保存");
};

const remove = async (id) => {
  const idx = addresses.value.findIndex(a => a.id === id);
  if (idx === -1) return;
  addresses.value.splice(idx, 1);
  try { localStorage.setItem('addresses', JSON.stringify(addresses.value)); } catch (e) { console.warn(e); }
  ElMessage.success("地址已删除");
};

const setDefault = async (id) => {
  addresses.value.forEach(a => { a.isDefault = (a.id === id); });
  try { localStorage.setItem('addresses', JSON.stringify(addresses.value)); } catch (e) { console.warn(e); }
  ElMessage.success("已设为默认地址");
};

const handleUpload = async (options) => {
  const res = await uploadImage(options.file);
  profileForm.avatarUrl = res.data;
  ElMessage.success("上传成功");
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

const handleLogout = async () => {
  await logout();
  authStore.clearSession();
  router.push("/login");
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

@media (max-width: 600px) {
  .addr-row { flex-direction: column; }
  .addr-cols { flex-direction: row; }
  .addr-left { width: 86px; }
  .addr-actions { margin-top: 8px; }
}
</style>
