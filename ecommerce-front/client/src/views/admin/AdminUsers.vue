<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">用户管理</div>
      </template>
      <el-table :data="users" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>禁用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const users = [
  { id: 1, email: "admin@local", nickname: "admin", statusText: "正常", lastLoginAt: "2026-05-19 09:12" },
  { id: 2, email: "user1@test.com", nickname: "user1", statusText: "正常", lastLoginAt: "2026-05-18 21:06" },
  { id: 3, email: "user2@test.com", nickname: "user2", statusText: "正常", lastLoginAt: "2026-05-18 18:34" }
];
</script>

<style scoped>
.card-header { font-weight: 600; }
</style>
