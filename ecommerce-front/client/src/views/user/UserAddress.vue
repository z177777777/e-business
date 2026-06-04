<template>
  <div class="page-card">
    <h2>地址管理</h2>
    <el-button type="primary" color="#ff6a3d" @click="openNewAddress" style="margin-bottom:12px">新增收货地址</el-button>

    <div v-if="addresses.length === 0" class="empty-hint">暂无收货地址，点击上方按钮新增</div>

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
              <div class="value">{{ addr.name }} <span v-if="addr.isDefault" class="default-tag">（默认）</span></div>
              <div class="value">{{ addr.phone }}</div>
              <div class="value">{{ formatAddress(addr) }}</div>
            </div>
          </div>
        </div>
        <div class="addr-actions">
          <el-button size="small" @click="startEdit(addr)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(addr.id)">删除</el-button>
          <el-button v-if="!addr.isDefault" size="small" @click="setDefault(addr.id)">设为默认</el-button>
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
          <el-cascader v-model="addrForm.regionPath" :options="regionOptions" filterable clearable placeholder="请选择省 / 市 / 区" style="width:100%" />
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
        <el-button type="primary" color="#ff6a3d" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { regionOptions } from "@/data/regions";
import { listAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from "@/api/address";

const addresses = ref([]);
const addrVisible = ref(false);
const addrForm = reactive({ id: null, name: "", phone: "", regionPath: [], detail: "", full: "", isDefault: false });

const formatAddress = (addr) => {
  const regionText = Array.isArray(addr.regionPath) && addr.regionPath.length ? addr.regionPath.join(" / ") : addr.regionText || "";
  const detailText = addr.detail || addr.full || "";
  return [regionText, detailText].filter(Boolean).join(" ");
};

const loadAddresses = async () => {
  try { const res = await listAddresses(); addresses.value = res.data || []; } catch (e) { addresses.value = []; }
};

onMounted(loadAddresses);

const openNewAddress = () => {
  addrForm.id = null; addrForm.name = ""; addrForm.phone = ""; addrForm.regionPath = []; addrForm.detail = ""; addrForm.full = ""; addrForm.isDefault = false;
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
    ElMessage.warning("请填写完整地址信息"); return;
  }
  const regionText = addrForm.regionPath.join(" / ");
  const full = [regionText, addrForm.detail].filter(Boolean).join(" ");
  try {
    if (addrForm.id) {
      await updateAddress(addrForm.id, { ...addrForm, regionText, full });
      ElMessage.success("地址已更新");
    } else {
      await createAddress({ ...addrForm, regionText, full });
      ElMessage.success("地址已新增");
    }
    if (addrForm.isDefault) await setDefaultAddress(addrForm.id);
  } catch (e) { ElMessage.error("保存地址失败"); }
  finally { addrVisible.value = false; await loadAddresses(); }
};

const remove = async (id) => {
  try {
    await ElMessageBox.confirm("确认删除该地址？", "删除地址", {
      confirmButtonText: "删除", cancelButtonText: "取消", type: "warning",
      customClass: "pretty-confirm-box pretty-confirm-box--danger", distinguishCancelAndClose: true, center: true
    });
  } catch (e) { return; }
  try { await deleteAddress(id); ElMessage.success("地址已删除"); await loadAddresses(); }
  catch (e) { ElMessage.error("删除失败"); }
};

const setDefault = async (id) => {
  try { await setDefaultAddress(id); ElMessage.success("已设为默认地址"); await loadAddresses(); }
  catch (e) { ElMessage.error("设置默认地址失败"); }
};
</script>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }
.empty-hint { padding:40px 0; text-align:center; color:var(--text-secondary); }
.addr-list { display:block; }
.addr-row { display:flex; justify-content:space-between; align-items:flex-start; padding:12px 0; border-bottom:1px solid #f0f0f0; }
.addr-main { flex:1; min-width:0; }
.addr-cols { display:flex; }
.addr-left { width:96px; padding-right:8px; text-align:right; color:var(--text-secondary); flex-shrink:0; }
.addr-left .label { margin:4px 0; }
.addr-right { flex:1; padding-left:8px; }
.addr-right .value { margin:4px 0; word-break:break-word; }
.default-tag { color:#ff6a3d; font-weight:600; }
.addr-actions { display:flex; gap:8px; margin-left:12px; flex-shrink:0; }
@media (max-width:600px) { .addr-row { flex-direction:column; } .addr-cols { flex-direction:row; } .addr-left { width:86px; } .addr-actions { margin-top:8px; } }
</style>
