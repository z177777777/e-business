USE ecommerce;

INSERT INTO products (name, subtitle, description, price, sold_count, cover_url, category, slug, is_hot, is_new, is_published, is_demo)
VALUES
('降噪蓝牙耳机 Pro','长续航降噪','适合通勤和学习场景，佩戴轻便。',299.00,1500,'https://loremflickr.com/1200/900/earbuds,wireless,headphone','数码潮玩','digital-earbuds-pro',1,1,1,0),
('4K 运动相机','防抖广角','户外骑行与旅行拍摄更稳定。',799.00,920,'https://loremflickr.com/1200/900/action,camera,gopro','数码潮玩','digital-action-cam-4k',0,1,1,0),

('维C亮肤精华','提亮肤色','轻透配方，日常护肤易吸收。',139.00,2100,'https://loremflickr.com/1200/900/serum,skincare,beauty','美妆护肤','beauty-vitc-serum',0,1,1,0),
('清透防晒棒 SPF50+','便携补涂','外出补涂不花妆，清爽不黏。',89.00,1750,'https://loremflickr.com/1200/900/sunscreen,skincare,spf','美妆护肤','beauty-sun-stick-spf50',1,1,1,0),

('感应智能垃圾桶','自动开合','静音开盖，适合卧室与书房。',219.00,860,'https://loremflickr.com/1200/900/smart,trash,bin','日用家居','home-sensor-bin',0,0,1,0),
('大豆蜡香薰机','柔和扩香','稳定扩香，营造舒适居家氛围。',159.00,740,'https://loremflickr.com/1200/900/diffuser,aroma,candle','日用家居','home-soy-aroma-diffuser',0,1,1,0),

('防泼水通勤托特包','大容量','可装笔记本与日常随身物品。',199.00,1330,'https://loremflickr.com/1200/900/tote,bag,fashion','服饰鞋包','fashion-commute-tote',0,0,1,0),
('轻弹针织休闲鞋','久走不累','透气鞋面，城市通勤百搭。',269.00,980,'https://loremflickr.com/1200/900/sneakers,shoes,knit','服饰鞋包','fashion-knit-sneaker',1,1,1,0),

('碳纤维登山杖','可调节','轻量稳固，适合徒步登山。',179.00,560,'https://loremflickr.com/1200/900/trekking,pole,hiking','运动户外','outdoor-carbon-pole',0,0,1,0),
('速干运动毛巾','吸汗快干','高效吸汗，健身露营都适用。',39.00,1900,'https://loremflickr.com/1200/900/towel,sports,gym','运动户外','outdoor-quickdry-towel',1,1,1,0),

('冷萃咖啡液','即冲即饮','口感顺滑，冷饮热饮都合适。',49.00,1580,'https://loremflickr.com/1200/900/coffee,cold,brew','食品饮品','food-coldbrew-concentrate',0,1,1,0),
('高蛋白燕麦脆','低糖轻食','早餐代餐和运动加餐都方便。',59.00,1220,'https://loremflickr.com/1200/900/granola,cereal,oat','食品饮品','food-protein-granola',0,0,1,0),

('小型空气炸锅','一键菜单','少油烹饪，适合 1-2 人家庭。',399.00,840,'https://loremflickr.com/1200/900/airfryer,kitchen,cooking','智能家电','appliance-mini-airfryer',1,1,1,0),
('桌面循环风扇','静音节能','三档风速，办公场景更舒适。',129.00,1110,'https://loremflickr.com/1200/900/desk,fan,circulator','智能家电','appliance-desk-circulation-fan',0,0,1,0),

('可擦写线圈笔记本','重复书写','适合会议记录与学习复盘。',45.00,970,'https://loremflickr.com/1200/900/notebook,spiral,stationery','办公文具','office-reusable-notebook',0,1,1,0),
('人体工学垂直鼠标','缓解手腕压力','长时间办公使用更舒适。',159.00,890,'https://loremflickr.com/1200/900/mouse,ergonomic,computer','办公文具','office-vertical-mouse',1,0,1,0)
;