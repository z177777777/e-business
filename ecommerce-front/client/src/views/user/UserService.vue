<template>
  <div class="page-card">
    <h2>客服服务</h2>
    <div class="service-info">
      <el-card shadow="hover" class="service-card">
        <h3>常见问题</h3>
        <ul>
          <li>如何下单？</li>
          <li>如何支付？</li>
          <li>如何申请退款？</li>
          <li>如何查看物流？</li>
        </ul>
      </el-card>
      <el-card shadow="hover" class="service-card">
        <h3>联系方式</h3>
        <p>客服邮箱：support@ebusiness.com</p>
        <p>服务时间：工作日 9:00 - 18:00</p>
        <p>客服热线：400-123-4567</p>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }
.service-info { display:grid; grid-template-columns:repeat(auto-fit, minmax(280px, 1fr)); gap:16px; }
.service-card h3 { margin:0 0 12px; font-size:16px; color:var(--text-primary); }
.service-card ul { margin:0; padding-left:20px; color:var(--text-secondary); }
.service-card ul li { margin:6px 0; }
.service-card p { margin:6px 0; color:var(--text-secondary); }

@media (max-width: 768px) {
  .page-card { padding: 12px !important; border-radius: 10px; }
  .page-card h2 { font-size: 18px; margin-bottom: 14px; }
  .service-info { grid-template-columns: 1fr !important; gap: 10px; }
  .service-card h3 { font-size: 15px; }
  .service-card ul li { font-size: 13px; }
  .service-card p { font-size: 13px; }
}
</style>
