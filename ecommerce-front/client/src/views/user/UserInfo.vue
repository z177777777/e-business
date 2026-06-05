<template>
  <div class="page-card">
    <h2>个人信息</h2>
    <el-form :model="profileForm" label-position="top" style="max-width:480px">
      <el-form-item label="昵称">
        <el-input v-model="profileForm.nickname" />
      </el-form-item>
      <el-form-item label="头像">
        <div class="avatar-wrapper">
          <div class="avatar-preview">
            <img v-if="profileForm.avatarUrl" :src="profileForm.avatarUrl" alt="avatar" />
            <div v-else class="avatar-placeholder"></div>
          </div>
          <el-upload class="upload-demo" :show-file-list="false" :http-request="handleUpload">
            <el-button>上传头像</el-button>
          </el-upload>
        </div>
      </el-form-item>
      <el-button type="primary" color="#ff6a3d" :loading="saving" @click="save">保存资料</el-button>
    </el-form>

    <el-dialog title="裁剪头像" v-model="cropVisible" width="420px" :close-on-click-modal="false">
      <div class="crop-container" ref="cropContainerRef"
        @mousedown="onCropMouseDown" @mousemove="onCropMouseMove" @mouseup="onCropMouseUp" @mouseleave="onCropMouseUp"
        @touchstart.prevent="onCropTouchStart" @touchmove.prevent="onCropTouchMove" @touchend="onCropMouseUp">
        <img v-if="cropImgSrc" :src="cropImgSrc" class="crop-image" ref="cropImgRef"
          :style="{ transform: `translate(${cropX}px, ${cropY}px) scale(${cropScale})` }"
          @load="onCropImgLoad" draggable="false" />
        <div class="crop-overlay"></div>
        <div class="crop-border"></div>
      </div>
      <div style="display:flex;align-items:center;gap:12px;margin-top:16px;padding:0 4px">
        <span style="font-size:12px;color:var(--text-secondary);white-space:nowrap">缩放</span>
        <el-slider v-model="cropScale" :min="0.2" :max="5" :step="0.01" style="flex:1" />
        <el-button size="mini" @click="resetCrop">重置</el-button>
      </div>
      <template #footer>
        <el-button @click="cropVisible = false">取消</el-button>
        <el-button type="primary" color="#ff6a3d" :loading="cropping" @click="confirmCrop">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getProfile, updateProfile } from "@/api/user";
import { uploadImage } from "@/api/files";
import { useAuthStore } from "@/store/auth";

const authStore = useAuthStore();

const profileForm = reactive({ nickname: "", avatarUrl: "" });
const saving = ref(false);

onMounted(async () => {
  const res = await getProfile();
  profileForm.nickname = res.data.nickname || "";
  profileForm.avatarUrl = res.data.avatarUrl || "";
});

const save = async () => {
  saving.value = true;
  try {
    const res = await updateProfile({ nickname: profileForm.nickname, avatarUrl: profileForm.avatarUrl });
    authStore.updateUser(res.data);
    ElMessage.success("资料已更新");
  } finally {
    saving.value = false;
  }
};

const handleUpload = async (options) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    cropImgSrc.value = e.target.result;
    imgW.value = 0; imgH.value = 0;
    cropVisible.value = true;
    cropX.value = 0; cropY.value = 0;
  };
  reader.readAsDataURL(options.file);
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
  } else { cropScale.value = 1; }
  cropX.value = 0; cropY.value = 0;
};

const isDragging = ref(false);
const dragStartX = ref(0);
const dragStartY = ref(0);

const onCropMouseDown = (e) => { isDragging.value = true; dragStartX.value = e.clientX - cropX.value; dragStartY.value = e.clientY - cropY.value; };
const onCropMouseMove = (e) => { if (!isDragging.value) return; cropX.value = e.clientX - dragStartX.value; cropY.value = e.clientY - dragStartY.value; };
const onCropMouseUp = () => { isDragging.value = false; };
const onCropTouchStart = (e) => { if (e.touches.length === 1) { isDragging.value = true; dragStartX.value = e.touches[0].clientX - cropX.value; dragStartY.value = e.touches[0].clientY - cropY.value; } };
const onCropTouchMove = (e) => { if (!isDragging.value || e.touches.length !== 1) return; cropX.value = e.touches[0].clientX - dragStartX.value; cropY.value = e.touches[0].clientY - dragStartY.value; };

const confirmCrop = async () => {
  cropping.value = true;
  try {
    const canvas = document.createElement("canvas");
    canvas.width = CROP_SIZE; canvas.height = CROP_SIZE;
    const ctx = canvas.getContext("2d");
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.src = cropImgSrc.value;
    await new Promise((resolve, reject) => { img.onload = resolve; img.onerror = reject; });
    const s = cropScale.value;
    ctx.setTransform(s, 0, 0, s, cropX.value + 140, cropY.value + 140);
    ctx.drawImage(img, 0, 0);
    const out = document.createElement("canvas");
    out.width = OUT_SIZE; out.height = OUT_SIZE;
    const outCtx = out.getContext("2d");
    outCtx.beginPath(); outCtx.arc(OUT_SIZE / 2, OUT_SIZE / 2, OUT_SIZE / 2, 0, Math.PI * 2); outCtx.clip();
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
</script>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }

.avatar-wrapper { display:flex; flex-direction:column; align-items:flex-start; gap:16px; }
.avatar-preview { width:100px; height:100px; border-radius:50%; overflow:hidden; border:3px solid #f0f0f0; }
.avatar-preview img { width:100%; height:100%; object-fit:cover; display:block; }
.avatar-placeholder { width:100%; height:100%; background:#e5e7eb; }

.crop-container { width:320px; height:320px; margin:0 auto; position:relative; overflow:hidden; border-radius:4px; background:#1a1a1a; cursor:grab; user-select:none; -webkit-user-select:none; }
.crop-container:active { cursor:grabbing; }
.crop-image { position:absolute; left:50%; top:50%; transform-origin:0 0; max-width:none; pointer-events:none; }
.crop-overlay { position:absolute; inset:0; border-radius:50%; box-shadow:0 0 0 9999px rgba(0,0,0,0.55); }
.crop-border { position:absolute; inset:20px; border:2px dashed rgba(255,255,255,0.7); border-radius:50%; pointer-events:none; }

@media (max-width: 768px) {
  .page-card { padding: 12px !important; border-radius: 10px; }
  .page-card h2 { font-size: 18px; margin-bottom: 14px; }
  .el-form-item { display: block; margin-bottom: 14px; }
  .el-form-item__label { width: 100% !important; text-align: left !important; padding-bottom: 4px; }
  .el-form-item__content { width: 100% !important; margin-left: 0 !important; }
  .el-input, .el-select, .el-button { width: 100% !important; }
  .avatar-wrapper { flex-direction: column; align-items: center; }
  .crop-container { width: 280px; height: 280px; }
}
</style>
