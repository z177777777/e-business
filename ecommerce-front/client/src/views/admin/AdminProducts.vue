<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
          <div style="display:flex;align-items:center;justify-content:space-between">
            <div style="display:flex;align-items:center;gap:12px;">
              <div class="card-header">商品管理</div>
              <el-select v-model="filterCategory" placeholder="全部种类" clearable size="small" style="width:140px;" @change="onSelectionChange([])">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </div>
            <div>
              <el-button type="primary" @click="openCreate">新建商品</el-button>
            </div>
          </div>
        </template>
        <el-table :data="filteredProducts" border style="width: 100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column type="index" label="ID" width="80" :index="indexMethod" />
        <el-table-column prop="name" label="商品名称" min-width="220">
          <template #default="{ row }">
            <span
              :class="['product-name-link', { 'has-reviews': row.hasReviews, 'has-new-reviews': row.hasNewReviews }]"
              @click="openReviews(row)"
            >{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" min-width="140" />
        <el-table-column prop="price" label="价格" width="120" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="restockRequests" label="催货" width="70" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px; display:flex; gap:8px; align-items:center;">
        <el-button type="warning" :disabled="!selected.length" @click="handleBatchUnpublish">批量下架</el-button>
        <el-button type="danger" :disabled="!selected.length" @click="handleBatchDelete">批量删除</el-button>
        <div style="flex:1"></div>
        <span style="color:var(--text-secondary); font-size:13px;">已选：{{ selected.length }} 项</span>
      </div>
    </el-card>
    
    <el-dialog :model-value="showDialog" title="商品" width="720px" @close="closeDialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="价格">
          <!-- 管理员每次调整按 1 单位变动（非 0.01） -->
          <el-input-number v-model="form.price" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" filterable allow-create clearable placeholder="请选择或输入分类">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <div style="display:flex;gap:8px;align-items:flex-start;width:100%">
            <el-input v-model="form.coverUrl" placeholder="输入图片 URL 或点击右侧上传本地图片" style="flex:1" />
            <el-upload
              :http-request="handleCoverUpload"
              :show-file-list="false"
              accept="image/jpeg,image/png,image/gif,image/webp"
            >
              <el-button :loading="uploading">本地上传</el-button>
            </el-upload>
          </div>
          <div v-if="form.coverUrl" style="margin-top:6px">
            <img :src="form.coverUrl" style="max-width:200px;max-height:120px;object-fit:contain;border:1px solid #e5e7eb;border-radius:6px;background:#fff" />
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="催上货次数">
          <el-input-number v-model="form.restockRequests" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="已发布">
          <el-switch v-model="form.isPublished" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- Reviews Dialog -->
    <el-dialog v-model="reviewVisible" title="商品评价" width="680px">
      <div v-if="reviewList.length === 0" style="text-align:center;padding:32px;color:var(--text-secondary)">暂无评价</div>
      <div v-else class="review-list">
        <div v-for="r in reviewList" :key="r.id" class="review-row">
          <div class="review-meta">
            <span>用户: {{ r.nickname || '匿名用户' }}</span>
            <el-rate :model-value="r.rating" disabled size="small" />
            <span style="font-size:12px;color:var(--text-secondary)">{{ formatReviewTime(r.createdAt) }}</span>
          </div>
          <div class="review-text">{{ r.content }}</div>
          <div v-if="r.imageUrls" style="display:flex;gap:6px;flex-wrap:wrap">
            <img v-for="(img, idx) in parseReviewImages(r.imageUrls)" :key="idx" :src="img" style="width:48px;height:48px;object-fit:cover;border-radius:4px;border:1px solid #eee" />
          </div>
          <div style="margin-top:6px;text-align:right">
            <el-button type="danger" size="mini" @click="deleteReview(r.id)">删除评价</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import http from "@/api/http";
import { uploadImage } from "@/api/files";

const route = useRoute();
const products = ref([]);
const selected = ref([]);
const filterCategory = ref("");

const filteredProducts = computed(() => {
  if (!filterCategory.value) return products.value;
  return products.value.filter(p => p.category === filterCategory.value);
});

const onSelectionChange = (rows) => {
  selected.value = Array.isArray(rows) ? rows : [];
};

// 默认包含的八个分类，确保管理员在新增/编辑时可以选择到全部类别
const ALL_CATEGORIES = [
  "数码潮玩",
  "美妆护肤",
  "日用家居",
  "服饰鞋包",
  "运动户外",
  "食品饮品",
  "智能家电",
  "办公文具"
];
const categories = ref([...ALL_CATEGORIES]);
const showDialog = ref(false);
const editingId = ref(null);
const form = ref({ id: null, name: "", subtitle: "", description: "", price: 0, category: "", coverUrl: "", isPublished: true, slug: "", stock: 10, restockRequests: 0 });
const uploading = ref(false);

const handleCoverUpload = async (options) => {
  uploading.value = true;
  try {
    const res = await uploadImage(options.file);
    form.value.coverUrl = res.data;
    ElMessage.success("上传成功");
  } catch (e) {
    ElMessage.error("上传失败");
  } finally {
    uploading.value = false;
  }
};

const indexMethod = (index) => index + 1;

// Reviews management
const reviewVisible = ref(false);
const reviewProductId = ref(null);
const reviewList = ref([]);

const openReviews = async (row) => {
  reviewProductId.value = row.id;
  try {
    const resp = await http.get(`/api/admin/products/${row.id}/reviews`);
    reviewList.value = resp.data || [];
  } catch (e) {
    reviewList.value = [];
  }
  reviewVisible.value = true;
  // Mark as viewed and refresh to remove flash
  try {
    await http.post(`/api/admin/products/${row.id}/mark-reviews-viewed`);
    row.hasNewReviews = false;
  } catch (e) { /* ignore */ }
};

const deleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm("该评价将被永久删除，删除后无法恢复。", "确认删除评价", {
      confirmButtonText: "确认删除",
      cancelButtonText: "暂不删除",
      type: "warning",
      customClass: "pretty-confirm-box pretty-confirm-box--danger",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  try {
    await http.delete(`/api/admin/reviews/${reviewId}`);
    ElMessage.success("评价已删除");
    reviewList.value = reviewList.value.filter(r => r.id !== reviewId);
  } catch (e) {
    ElMessage.error("删除评价失败");
  }
};

const parseReviewImages = (imageUrls) => {
  if (!imageUrls) return [];
  if (Array.isArray(imageUrls)) return imageUrls;
  try { return JSON.parse(imageUrls); } catch (e) { return []; }
};

const formatReviewTime = (v) => (v ? new Date(v).toLocaleString() : "");

const loadProducts = async () => {
  try {
    const resp = await http.get("/api/admin/products");
    const data = resp.data || [];
    // 保持后端价格原样，不做额外四舍五入或缩放
    // 归一化 subtitle 字段（兼容不同后端字段命名）以确保编辑时能显示已有副标题
    products.value = Array.isArray(data)
      ? data.map(p => {
          const subtitle = p.subtitle ?? p.subTitle ?? p.shortSubtitle ?? (p.description ? String(p.description).split('\n')[0] : "");
          return { ...p, price: p.price, isPublished: p.isPublished, subtitle };
        }).sort((a, b) => Number(a.id || 0) - Number(b.id || 0))
      : [];
    // populate categories from current products and merge with ALL_CATEGORIES
    const set = new Set(ALL_CATEGORIES);
    products.value.forEach(p => { if (p.category) set.add(p.category); });
    categories.value = Array.from(set);
  } catch (e) {
    console.warn("load products failed", e);
  }
};

const openCreate = () => {
  editingId.value = null;
  form.value = { id: null, name: "", subtitle: "", description: "", price: 0, category: "", coverUrl: "", isPublished: true, slug: "", stock: 10, restockRequests: 0 };
  showDialog.value = true;
};

const openEdit = async (row) => {
  editingId.value = row.id;
  // 优先从后端拉取最新详情，确保字段完整（包括 subtitle）
  try {
  const resp = await http.get(`/api/products/${row.id}`);
    const data = resp.data || resp.data?.data || resp.data?.product || {};
    const subtitle = data.subtitle ?? data.subTitle ?? data.shortSubtitle ?? (data.description ? String(data.description).split('\n')[0] : row.subtitle || "");
    form.value = {
      id: row.id,
      name: data.name ?? row.name,
      subtitle,
      description: data.description ?? row.description ?? "",
      price: data.price ?? row.price ?? 0,
      category: data.category ?? row.category ?? "",
      coverUrl: data.coverUrl ?? data.image ?? row.coverUrl ?? "",
      isPublished: typeof data.isPublished === 'boolean' ? data.isPublished : !!row.isPublished,
      slug: data.slug ?? row.slug ?? "",
      stock: data.stock ?? row.stock ?? 10,
      restockRequests: data.restockRequests ?? row.restockRequests ?? 0
    };
  } catch (e) {
    // 回退到原始行数据，仍然保证 subtitle 字段存在
    form.value = { id: row.id, name: row.name, subtitle: row.subtitle || "", description: row.description || "", price: row.price || 0, category: row.category || "", coverUrl: row.coverUrl || "", isPublished: !!row.isPublished, slug: row.slug || "", stock: row.stock ?? 10, restockRequests: row.restockRequests ?? 0 };
  }
  showDialog.value = true;
};

const closeDialog = () => { showDialog.value = false; };

const saveProduct = async () => {
  try {
    ensureCategoryExists(form.value.category);
    if (editingId.value) {
      await http.put(`/api/admin/products/${editingId.value}`, form.value);
    } else {
      await http.post(`/api/admin/products`, form.value);
    }
    showDialog.value = false;
    await loadProducts();
    ElMessage.success('保存成功');
  } catch (e) {
    console.warn("save product failed", e);
  }
};

// if admin adds a new category via select, ensure it's tracked
const ensureCategoryExists = (cat) => {
  if (!cat) return;
  if (!categories.value.includes(cat)) categories.value.push(cat);
};

const togglePublish = async (row) => {
  try {
    const target = !row.isPublished;
    await http.post(`/api/admin/products/${row.id}/publish`, null, { params: { published: target } });
    await loadProducts();
    ElMessage.success(target ? '已上架' : '已下架');
  } catch (e) {
    console.warn("toggle publish failed", e);
  }
};

const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`商品「${row.name}」将从数据库中永久删除，删除后无法恢复。`, '确认删除商品', {
      confirmButtonText: '确认删除',
      cancelButtonText: '暂不删除',
      type: 'warning',
      customClass: 'pretty-confirm-box pretty-confirm-box--danger',
      distinguishCancelAndClose: true,
      center: true
    });
    await http.delete(`/api/admin/products/${row.id}`);
    await loadProducts();
    ElMessage.success('删除成功');
  } catch (e) {
    // 取消或错误时忽略（确认框取消会抛出）
    // 如果是其他错误，可以在控制台查看
    if (e && e.toString && e.toString().includes('Error')) {
      console.warn('delete product failed', e);
    }
  }
};

const handleBatchUnpublish = async () => {
  if (!selected.value.length) return;
  try {
    await ElMessageBox.confirm(`确认将所选 ${selected.value.length} 个商品下架吗？`, '批量下架', {
      confirmButtonText: '确认下架',
      cancelButtonText: '取消',
      type: 'warning',
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) { return; }
  let success = 0;
  for (const row of selected.value) {
    try {
      await http.post(`/api/admin/products/${row.id}/publish`, null, { params: { published: false } });
      success++;
    } catch (e) { /* skip */ }
  }
  ElMessage.success(`已下架 ${success} 件商品`);
  selected.value = [];
  await loadProducts();
};

const handleBatchDelete = async () => {
  if (!selected.value.length) return;
  try {
    await ElMessageBox.confirm(`所选 ${selected.value.length} 个商品将从数据库中永久删除，删除后无法恢复。`, '确认批量删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'pretty-confirm-box pretty-confirm-box--danger',
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) { return; }
  let success = 0;
  for (const row of selected.value) {
    try {
      await http.delete(`/api/admin/products/${row.id}`);
      success++;
    } catch (e) { /* skip */ }
  }
  ElMessage.success(`已删除 ${success} 件商品`);
  selected.value = [];
  await loadProducts();
};

onMounted(async () => {
  await loadProducts();
  const reviewPid = route.query.reviewProductId;
  if (reviewPid) {
    await nextTick();
    const target = products.value.find(p => String(p.id) === String(reviewPid));
    if (target) openReviews(target);
  }
});
</script>

<style scoped>
.card-header { font-weight: 600; }
.product-name-link { color: #111827; cursor: pointer; font-weight: 500; }
.product-name-link.has-reviews { color: #409eff; }
.product-name-link:hover { text-decoration: underline; }

.product-name-link.has-new-reviews {
  animation: reviewFlash 0.6s ease-in-out infinite alternate;
}

@keyframes reviewFlash {
  0% { color: #409eff; }
  50% { color: #ff6a3d; }
  100% { color: #409eff; }
}
.review-list { display: grid; gap: 12px; }
.review-row { padding: 12px; border: 1px solid #e5e7eb; border-radius: 10px; }
.review-meta { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.review-text { line-height: 1.6; color: #374151; }
</style>
