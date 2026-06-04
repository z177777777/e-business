-- ============================================================
-- 删除旧版产品 + 为新版产品更新封面图片
-- 使用 LoremFlickr 按关键词返回商品相关图片
-- ============================================================
USE ecommerce;

-- ==================== 删除旧版产品 ====================
DELETE FROM products WHERE name IN (
  '轻量蓝牙耳机',
  '智能手表',
  '便携投影仪',
  '水润粉底液',
  '氨基酸洁面',
  '修护面膜',
  '极简收纳盒',
  '智能保温杯',
  '城市通勤双肩包',
  '折叠露营椅',
  '轻量运动水壶',
'徒步腰包',
  '燕麦坚果礼盒',
  '低糖燕麦奶',
  '咖啡浓缩液',
  '多功能料理机',
  '无线吸尘器',
  '迷你咖啡机',
  '无线键鼠套装',
  '设计师笔记本',
  '桌面收纳架'
);

-- ==================== 数码潮玩 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/earbuds,wireless,headphone' WHERE name = '降噪蓝牙耳机 Pro';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/action,camera,gopro' WHERE name = '4K 运动相机';

-- ==================== 美妆护肤 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/serum,skincare,beauty' WHERE name = '维C亮肤精华';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/sunscreen,skincare,spf' WHERE name = '清透防晒棒 SPF50+';

-- ==================== 日用家居 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/smart,trash,bin' WHERE name = '感应智能垃圾桶';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/diffuser,aroma,candle' WHERE name = '大豆蜡香薰机';

-- ==================== 服饰鞋包 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/tote,bag,fashion' WHERE name = '防泼水通勤托特包';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/sneakers,shoes,knit' WHERE name = '轻弹针织休闲鞋';

-- ==================== 运动户外 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/trekking,pole,hiking' WHERE name = '碳纤维登山杖';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/towel,sports,gym' WHERE name = '速干运动毛巾';

-- ==================== 食品饮品 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/coffee,cold,brew' WHERE name = '冷萃咖啡液';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/granola,cereal,oat' WHERE name = '高蛋白燕麦脆';

-- ==================== 智能家电 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/airfryer,kitchen,cooking' WHERE name = '小型空气炸锅';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/desk,fan,circulator' WHERE name = '桌面循环风扇';

-- ==================== 办公文具 ====================
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/notebook,spiral,stationery' WHERE name = '可擦写线圈笔记本';
UPDATE products SET cover_url = 'https://loremflickr.com/1200/900/mouse,ergonomic,computer' WHERE name = '人体工学垂直鼠标';
