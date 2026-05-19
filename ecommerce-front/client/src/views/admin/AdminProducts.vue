<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">商品管理</div>
      </template>
      <el-table :data="products" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="220" />
        <el-table-column prop="category" label="分类" min-width="140" />
        <el-table-column prop="price" label="价格" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const products = [
  { id: 101, name: "无线蓝牙耳机", category: "数码", price: "￥199", status: "上架" },
  { id: 102, name: "简约风背包", category: "箱包", price: "￥159", status: "上架" },
  { id: 103, name: "轻薄笔记本支架", category: "办公", price: "￥89", status: "上架" }
];
</script>

<style scoped>
.card-header { font-weight: 600; }
</style>
