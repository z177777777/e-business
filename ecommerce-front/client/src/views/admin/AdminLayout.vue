<template>
  <div class="admin-layout">
    <header class="admin-header">
      <h2>管理员后台</h2>
    </header>
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: "AdminLayout"
};
</script>

<style scoped>
.admin-header { background:#2d3748; color:#fff; padding:12px 16px }
.admin-main { padding:16px }
</style>
