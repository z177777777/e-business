<template>
  <div class="checkout-page">
    <div class="checkout-shell">
      <h2>结算</h2>

      <div class="address-section">
        <h3>选择收货地址</h3>
        <el-radio-group v-model="selectedId">
          <div v-for="addr in addresses" :key="addr.id" class="addr-row">
            <el-radio :label="addr.id">
              <div class="addr-item">
                <div class="addr-main">
                  <div class="addr-name">{{ addr.name }} {{ addr.phone }}</div>
                  <div class="addr-text">{{ addr.full }}</div>
                </div>
                <div class="addr-actions">
                  <el-button size="mini" @click="openEdit(addr)">编辑</el-button>
                  <el-button size="mini" type="danger" @click="remove(addr.id)">删除</el-button>
                </div>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
        <el-button type="primary" @click="openNew">新增收货地址</el-button>
      </div>

      <div class="order-summary">
        <h3>订单信息</h3>
        <p>（演示）订单项来自购物车，结算会使用所选地址。</p>
        <el-button type="success" :disabled="!selectedId" @click="confirmCheckout">提交订单</el-button>
      </div>

      <el-dialog title="收货地址" :visible.sync="editingVisible">
        <el-form :model="form" label-position="top">
          <el-form-item label="收件人">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input v-model="form.full" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editingVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { listAddresses, createAddress, updateAddress, deleteAddress } from "@/api/address";
import { ElMessage } from "element-plus";

const addresses = ref([]);
const selectedId = ref("");

const editingVisible = ref(false);
const editingId = ref(null);
const form = ref({ name: "", phone: "", full: "" });

const load = async () => {
  const res = await listAddresses();
  addresses.value = res.data || [];
  if (!selectedId.value && addresses.value.length) selectedId.value = addresses.value.find(a=>a.isDefault)?.id || addresses.value[0].id;
};

const openNew = () => {
  editingId.value = null;
  form.value = { name: "", phone: "", full: "" };
  editingVisible.value = true;
};

const openEdit = (addr) => {
  editingId.value = addr.id;
  form.value = { name: addr.name, phone: addr.phone, full: addr.full };
  editingVisible.value = true;
};

const save = async () => {
  if (!form.value.name || !form.value.phone || !form.value.full) {
    ElMessage.warning("请填写完整地址信息");
    return;
  }
  if (editingId.value) {
    await updateAddress(editingId.value, form.value);
    ElMessage.success("地址已更新");
  } else {
    const res = await createAddress(form.value);
    ElMessage.success("地址已新增");
    selectedId.value = res.data.id;
  }
  editingVisible.value = false;
  await load();
};

const remove = async (id) => {
  await deleteAddress(id);
  ElMessage.success("已删除");
  if (selectedId.value === id) selectedId.value = "";
  await load();
};

const confirmCheckout = async () => {
  ElMessage.success(`已使用地址 ${selectedId.value} 结算（演示）`);
};

onMounted(load);
</script>

<style scoped>
.checkout-shell { padding: 20px; display: grid; gap: 16px }
.addr-row { margin-bottom: 8px }
.addr-item { display:flex; justify-content:space-between; align-items:center }
.addr-main { max-width: 70% }
.addr-name { font-weight:600 }
.addr-text { color: var(--text-secondary) }
.addr-actions { display:flex; gap:8px }
</style>
