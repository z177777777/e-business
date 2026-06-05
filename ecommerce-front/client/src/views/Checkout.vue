<template>
  <div class="checkout-page">
    <div class="checkout-shell">
      <h2>结算</h2>

      <div class="address-section">
        <h3>选择收货地址</h3>
        <div class="addr-list">
          <div
            v-for="(addr, index) in addresses"
            :key="getAddressKey(addr, index)"
            :class="['addr-row', { 'is-selected': selectedKey === getAddressKey(addr, index), 'is-default': addr.isDefault }]"
            @click="selectAddress(addr, index)"
          >
            <div class="addr-item">
              <div class="addr-radio-dot" aria-hidden="true">
                <span class="addr-radio-ring"></span>
              </div>
              <div class="addr-main">
                <div class="addr-cols">
                  <div class="addr-left">
                    <div class="label">收件人：</div>
                    <div class="label">手机：</div>
                    <div class="label">地址：</div>
                  </div>
                  <div class="addr-right">
                    <div class="value addr-name-line">
                      <span>{{ addr.name }}</span>
                      <el-tag v-if="addr.isDefault" size="small" type="danger" effect="plain">默认地址</el-tag>
                    </div>
                    <div class="value">{{ addr.phone }}</div>
                    <div class="value">{{ formatAddress(addr) }}</div>
                  </div>
                </div>
              </div>
              <div class="addr-actions" @click.stop>
                <el-button class="addr-action-btn" size="small" @click.stop="openEdit(addr)">编辑</el-button>
                <el-button class="addr-action-btn" size="small" type="danger" @click.stop="remove(addr.id)">删除</el-button>
                <el-button v-if="!addr.isDefault" class="addr-action-btn" size="small" @click.stop="setDefault(addr.id)">设为默认</el-button>
              </div>
            </div>
          </div>
          <div class="address-actions">
            <el-button type="primary" @click="openNew">新增收货地址</el-button>
          </div>
        </div>
      </div>

      <div class="order-summary">
        <h3>订单信息</h3>
        <p>（演示）订单项来自购物车，结算会使用所选地址。</p>
        <el-button type="success" :disabled="!selectedKey || submitting" :loading="submitting" @click="confirmCheckout">提交订单</el-button>
      </div>

      <el-dialog title="收货地址" v-model="editingVisible">
        <el-form :model="form" label-position="top">
          <el-form-item label="收件人">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="所在地区">
            <el-cascader
              v-model="form.regionPath"
              :options="regionOptions"
              filterable
              clearable
              placeholder="请选择省 / 市 / 区"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input v-model="form.detail" placeholder="街道、小区、门牌号等" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
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
import { computed, ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { regionOptions } from "@/data/regions";
import { getCartItems } from "@/api/cart";
import { listAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from "@/api/address";
import { checkoutOrder, listOrders } from "@/api/order";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();
const addresses = ref([]);
const selectedKey = ref("");
const selectedId = ref("");
const submitting = ref(false);
const cartItems = ref([]);

const selectedCartIds = computed(() => {
  const raw = route.query.items;
  if (Array.isArray(raw)) return raw.flatMap((value) => String(value).split(","));
  return String(raw || "").split(",");
});

const editingVisible = ref(false);
const editingId = ref(null);
const form = ref({ id: null, name: "", phone: "", regionPath: [], detail: "", full: "", isDefault: false });

const formatAddress = (addr) => {
  const regionText = Array.isArray(addr.regionPath) && addr.regionPath.length ? addr.regionPath.join(" / ") : addr.regionText || "";
  const detailText = addr.detail || addr.full || "";
  return [regionText, detailText].filter(Boolean).join(" ");
};

const getAddressKey = (addr, index) => String(addr?.id ?? index);

const normalizeOrderItems = (items) => {
  return (Array.isArray(items) ? items : [])
    .map((item) => ({
      productIdentifier: String(item?.productIdentifier || ""),
      quantity: Number(item?.quantity || 0)
    }))
    .filter((item) => item.productIdentifier && item.quantity > 0)
    .sort((left, right) => left.productIdentifier.localeCompare(right.productIdentifier) || left.quantity - right.quantity);
};

const sameItemList = (leftItems, rightItems) => {
  const left = normalizeOrderItems(leftItems);
  const right = normalizeOrderItems(rightItems);
  if (left.length !== right.length) return false;
  for (let index = 0; index < left.length; index += 1) {
    if (left[index].productIdentifier !== right[index].productIdentifier) return false;
    if (left[index].quantity !== right[index].quantity) return false;
  }
  return true;
};

const selectAddress = (addr, index) => {
  selectedKey.value = getAddressKey(addr, index);
  selectedId.value = addr?.id != null ? String(addr.id) : "";
};

const load = async () => {
  const res = await listAddresses();
  addresses.value = res.data || [];
  if (!selectedKey.value && addresses.value.length) {
    const firstIndex = addresses.value.findIndex((a) => a.isDefault);
    const index = firstIndex >= 0 ? firstIndex : 0;
    const first = addresses.value[index];
    selectedKey.value = getAddressKey(first, index);
    selectedId.value = first?.id != null ? String(first.id) : "";
  }
};

const loadSelectedCartItems = async () => {
  const res = await getCartItems();
  const data = res.data || {};
  const items = Array.isArray(data.items) ? data.items : [];
  const selectedIdSet = new Set(selectedCartIds.value.filter(Boolean));
  cartItems.value = items.filter((item) => selectedIdSet.size === 0 ? item.selected : selectedIdSet.has(String(item.id)));
};

const openNew = () => {
  editingId.value = null;
  form.value = { id: null, name: "", phone: "", regionPath: [], detail: "", full: "", isDefault: false };
  editingVisible.value = true;
};

const openEdit = (addr) => {
  editingId.value = addr.id;
  form.value = {
    id: addr.id,
    name: addr.name,
    phone: addr.phone,
    regionPath: Array.isArray(addr.regionPath) ? [...addr.regionPath] : [],
    detail: addr.detail || (!addr.regionPath ? addr.full || "" : ""),
    full: addr.full || "",
    isDefault: !!addr.isDefault
  };
  editingVisible.value = true;
};

const save = async () => {
  if (!form.value.name || !form.value.phone || !form.value.regionPath?.length || !form.value.detail) {
    ElMessage.warning("请填写完整地址信息");
    return;
  }
  const regionText = form.value.regionPath.join(" / ");
  const full = [regionText, form.value.detail].filter(Boolean).join(" ");
  if (editingId.value) {
    await updateAddress(editingId.value, { ...form.value, regionText, full });
    ElMessage.success("地址已更新");
  } else {
    const res = await createAddress({ ...form.value, regionText, full });
    ElMessage.success("地址已新增");
    selectedId.value = String(res.data.id);
    selectedKey.value = String(res.data.id);
  }
  if (form.value.isDefault) {
    await setDefaultAddress(editingId.value || selectedId.value);
  }
  editingVisible.value = false;
  await load();
};

const remove = async (id) => {
  const confirmed = await ElMessageBox.confirm("确认删除该地址吗？", "删除地址", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
    customClass: "pretty-confirm-box pretty-confirm-box--danger",
    distinguishCancelAndClose: true,
    center: true
  }).then(() => true).catch(() => false);
  if (!confirmed) return;
  await deleteAddress(id);
  ElMessage.success("已删除");
  if (selectedId.value === String(id)) selectedId.value = "";
  if (selectedKey.value === String(id)) selectedKey.value = "";
  await load();
};

const setDefault = async (id) => {
  await setDefaultAddress(id);
  ElMessage.success("已设为默认地址");
  await load();
};

const confirmCheckout = async () => {
  if (submitting.value) return;
  submitting.value = true;
  try {
    try {
      await loadSelectedCartItems();
    } catch (e) {
      cartItems.value = [];
    }
    // 在创建订单前，先检测是否存在最近未支付的重复订单，提示用户是否继续购买
    try {
      const listResPre = await listOrders();
      const ordersPre = listResPre.data || [];
      const nowPre = new Date();
      const currentItems = normalizeOrderItems(cartItems.value);
      if (currentItems.length === 0) {
        throw new Error("请选择要结算的商品");
      }

      // 检测最近未支付订单（10 分钟内）与已支付订单（24 小时内）
      const candidatePending = ordersPre
        .filter(o => o.status === "PENDING_PAYMENT")
        .map(o => ({ ...o, __created: o.createdAt ? new Date(o.createdAt) : null }))
        .filter(o => o.__created && (nowPre - o.__created) < 1000 * 60 * 10)
        .sort((a, b) => b.__created - a.__created)
        .find((order) => sameItemList(currentItems, order.items));
      const candidatePaid = ordersPre
        .filter(o => o.status === "PAID")
        .map(o => ({ ...o, __created: o.createdAt ? new Date(o.createdAt) : null }))
        .filter(o => o.__created && (nowPre - o.__created) < 1000 * 60 * 60 * 24)
        .sort((a, b) => b.__created - a.__created)
        .find((order) => sameItemList(currentItems, order.items));

      // 优先提示未支付订单（更紧急），否则提示已支付订单
      const candidatePre = candidatePending || candidatePaid;
      if (candidatePre) {
        const isPaid = candidatePre.status === "PAID";
        const tip = isPaid
          ? `检测到您在 24 小时内已支付过订单 ${candidatePre.orderNo}，是否仍要继续下单？`
          : `检测到已有相同订单仍未支付，是否继续购买？`;
        // 按钮调整为：确认=继续下单（继续创建新订单），取消=取消（跳转到已有订单）
        const confirmed = await ElMessageBox.confirm(tip, "检测到重复订单", {
          confirmButtonText: "继续下单",
          cancelButtonText: "取消",
          type: "warning",
          customClass: "pretty-confirm-box",
          distinguishCancelAndClose: true,
          center: true
        }).then(() => true).catch(() => false);
        if (!confirmed) {
          return;
        }
        // 用户确认 -> 继续下单（继续执行后续创建订单逻辑）
      }
    } catch (e) {
      // ignore pre-check errors and continue to create order
    }

    // 尝试创建订单
    const res = await checkoutOrder();
    const order = res.data;
    const active = addresses.value.find((addr, index) => getAddressKey(addr, index) === selectedKey.value);
    const label = active?.isDefault ? "默认地址" : "已选地址";
    ElMessage.success(`已使用${label}结算，订单 ${order.orderNo}（演示）`);
    if (order && order.id) {
      // 下单成功后跳转到订单详情页，让用户在详情页上点击“模拟支付”按钮
      await router.push(`/orders/${order.id}`);
    } else {
      await router.push(`/orders`);
    }
  } catch (err) {
    const msg = err?.message || "请求失败";
    ElMessage.error(msg);
  } finally {
    submitting.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.checkout-shell { padding: 20px; display: grid; gap: 16px }
.addr-list { display: grid; gap: 12px; }
.addr-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  width: 100%;
  padding: 18px 20px;
  min-height: 120px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease;
}
.addr-row:hover { border-color: #f56c6c; box-shadow: 0 4px 12px rgba(245, 108, 108, 0.08); }
.addr-row.is-selected { border-color: #f56c6c; background: #fffaf9; box-shadow: 0 4px 12px rgba(245, 108, 108, 0.12); }
.addr-item { display:flex; justify-content:space-between; align-items:flex-start; width: 100%; gap: 16px; }
.addr-radio-dot { width: 20px; padding-top: 4px; flex-shrink: 0; display: flex; justify-content: center; }
.addr-radio-ring { width: 14px; height: 14px; border-radius: 50%; border: 2px solid #c0c4cc; background: #fff; box-sizing: border-box; }
.addr-row.is-selected .addr-radio-ring { border-color: #f56c6c; box-shadow: inset 0 0 0 3px #fff; background: #f56c6c; }
.addr-main { flex: 1; min-width: 0; }
.addr-cols { display: flex; align-items: flex-start; gap: 12px; }
.addr-left { width: 104px; padding-right: 6px; text-align: right; color: var(--text-secondary); flex-shrink: 0; line-height: 2; }
.addr-left .label { margin: 2px 0; }
.addr-right { flex: 1; padding-left: 0; line-height: 2; }
.addr-right .value { margin: 2px 0; word-break: break-word; }
.addr-name-line { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.addr-actions { display:flex; gap:6px; margin-left: 12px; flex-shrink: 0; padding-top: 2px; align-items: flex-start; flex-wrap: wrap; }
.addr-action-btn { height: 28px; padding: 0 10px; font-size: 12px; line-height: 26px; }
.address-actions { margin-top: 4px; }

.addr-row { cursor: pointer; }

@media (max-width: 600px) {
  .addr-row { flex-direction: column; gap: 12px; padding: 16px 16px; min-height: auto; }
  .addr-item { gap: 12px; }
  .addr-cols { flex-direction: row; }
  .addr-left { width: 86px; }
  .addr-actions { margin-top: 2px; margin-left: 0; flex-wrap: wrap; }
  .addr-action-btn { height: 26px; padding: 0 8px; font-size: 12px; }
  .address-actions { margin-top: 8px; }
}

@media (max-width: 768px) {
  .checkout-shell { padding: 8px !important; gap: 12px; }
  .checkout-shell h2 { font-size: 22px; }
  .addr-row { flex-direction: column; gap: 12px; padding: 16px; min-height: auto; }
  .addr-item { gap: 12px; flex-direction: column; }
  .addr-cols { flex-direction: column; }
  .addr-left { width: auto; }
  .addr-left .label { font-size: 12px; }
  .addr-right .value { font-size: 13px; }
  .addr-actions { margin-top: 4px; margin-left: 0; flex-wrap: wrap; gap: 4px; }
  .addr-action-btn { height: 26px; padding: 0 8px; font-size: 12px; }
  .address-actions .el-button { width: 100%; }
  .order-summary h3 { font-size: 16px; }
  .order-summary p { font-size: 13px; }
  .checkout-btn { width: 100%; }
}
</style>
