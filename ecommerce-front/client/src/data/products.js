const productList = [
  {
    id: "earbuds-lite",
    name: "轻量蓝牙耳机",
    subtitle: "低延迟",
    sold: "1.2w+",
    price: 199,
    category: "数码潮玩",
    tag: "爆款",
    bg: "linear-gradient(135deg, #dff9fb, #c7ecee)"
  },
  {
    id: "smart-watch",
    name: "智能手表",
    subtitle: "多运动模式",
    sold: "8600+",
    price: 399,
    category: "数码潮玩",
    bg: "linear-gradient(135deg, #f6e58d, #f9ca24)"
  },
  {
    id: "portable-projector",
    name: "便携投影仪",
    subtitle: "高清便携",
    sold: "5200+",
    price: 899,
    category: "数码潮玩",
    bg: "linear-gradient(135deg, #7ed6df, #22a6b3)"
  },
  {
    id: "foundation-liquid",
    name: "水润粉底液",
    subtitle: "轻薄服帖",
    sold: "6400+",
    price: 189,
    category: "美妆护肤",
    bg: "linear-gradient(135deg, #f8a5c2, #f78fb3)"
  },
  {
    id: "amino-cleanser",
    name: "氨基酸洁面",
    subtitle: "温和清洁",
    sold: "7800+",
    price: 99,
    category: "美妆护肤",
    bg: "linear-gradient(135deg, #f3a683, #f7d794)"
  },
  {
    id: "repair-mask",
    name: "修护面膜",
    subtitle: "补水修护",
    sold: "5600+",
    price: 69,
    category: "美妆护肤",
    bg: "linear-gradient(135deg, #ffd3b6, #ffaaa5)"
  },
  {
    id: "minimal-box",
    name: "极简收纳盒",
    subtitle: "桌面整洁",
    sold: "3900+",
    price: 49,
    category: "日用家居",
    bg: "linear-gradient(135deg, #c8d6e5, #8395a7)"
  },
  {
    id: "candle-set",
    name: "香氛蜡烛套装",
    subtitle: "治愈系香调",
    sold: "4100+",
    price: 129,
    category: "日用家居",
    bg: "linear-gradient(135deg, #f3a683, #f7d794)"
  },
  {
    id: "eye-lamp",
    name: "护眼台灯",
    subtitle: "柔光护眼",
    sold: "5300+",
    price: 249,
    category: "日用家居",
    bg: "linear-gradient(135deg, #63cdda, #3dc1d3)"
  },
  {
    id: "smart-thermos",
    name: "智能保温杯",
    subtitle: "恒温锁鲜",
    sold: "8600+",
    price: 159,
    category: "日用家居",
    tag: "热销",
    bg: "linear-gradient(135deg, #ffbe76, #ff7979)"
  },
  {
    id: "light-windbreaker",
    name: "轻薄风衣",
    subtitle: "新季配色",
    sold: "2200+",
    price: 399,
    category: "服饰鞋包",
    bg: "linear-gradient(135deg, #f8a5c2, #f78fb3)"
  },
  {
    id: "commute-backpack",
    name: "城市通勤双肩包",
    subtitle: "多仓收纳",
    sold: "9800+",
    price: 329,
    category: "服饰鞋包",
    tag: "推荐",
    bg: "linear-gradient(135deg, #f6e58d, #f9ca24)"
  },
  {
    id: "running-shoes",
    name: "轻量跑鞋",
    subtitle: "城市训练",
    sold: "4300+",
    price: 529,
    category: "服饰鞋包",
    bg: "linear-gradient(135deg, #778beb, #546de5)"
  },
  {
    id: "camp-chair",
    name: "折叠露营椅",
    subtitle: "户外休闲",
    sold: "2600+",
    price: 188,
    category: "运动户外",
    bg: "linear-gradient(135deg, #60a3bc, #3c6382)"
  },
  {
    id: "sport-bottle",
    name: "轻量运动水壶",
    subtitle: "随身携带",
    sold: "3100+",
    price: 69,
    category: "运动户外",
    bg: "linear-gradient(135deg, #1dd1a1, #10ac84)"
  },
  {
    id: "hiking-belt",
    name: "徒步腰包",
    subtitle: "防水耐磨",
    sold: "1800+",
    price: 139,
    category: "运动户外",
    bg: "linear-gradient(135deg, #f8c291, #e58e26)"
  },
  {
    id: "oat-gift",
    name: "燕麦坚果礼盒",
    subtitle: "轻食组合",
    sold: "5200+",
    price: 79,
    category: "食品饮品",
    bg: "linear-gradient(135deg, #f6e58d, #f9ca24)"
  },
  {
    id: "oat-milk",
    name: "低糖燕麦奶",
    subtitle: "早餐好物",
    sold: "3400+",
    price: 39,
    category: "食品饮品",
    bg: "linear-gradient(135deg, #f3a683, #f7d794)"
  },
  {
    id: "coffee-concentrate",
    name: "咖啡浓缩液",
    subtitle: "随冲随饮",
    sold: "2600+",
    price: 59,
    category: "食品饮品",
    bg: "linear-gradient(135deg, #ffbe76, #ff7979)"
  },
  {
    id: "multi-cooker",
    name: "多功能料理机",
    subtitle: "效率厨房",
    sold: "3900+",
    price: 699,
    category: "智能家电",
    bg: "linear-gradient(135deg, #f6b93b, #fa983a)"
  },
  {
    id: "cordless-vacuum",
    name: "无线吸尘器",
    subtitle: "轻松清洁",
    sold: "2100+",
    price: 899,
    category: "智能家电",
    bg: "linear-gradient(135deg, #82ccdd, #60a3bc)"
  },
  {
    id: "mini-coffee-machine",
    name: "迷你咖啡机",
    subtitle: "便携冲煮",
    sold: "5400+",
    price: 499,
    category: "智能家电",
    tag: "人气",
    bg: "linear-gradient(135deg, #7ed6df, #22a6b3)"
  },
  {
    id: "smart-lamp",
    name: "智能台灯",
    subtitle: "护眼柔光",
    sold: "4200+",
    price: 249,
    category: "智能家电",
    bg: "linear-gradient(135deg, #63cdda, #3dc1d3)"
  },
  {
    id: "wireless-kit",
    name: "无线键鼠套装",
    subtitle: "桌面效率",
    sold: "3800+",
    price: 169,
    category: "办公文具",
    bg: "linear-gradient(135deg, #e6efff, #c6d9ff)"
  },
  {
    id: "designer-notebook",
    name: "设计师笔记本",
    subtitle: "灵感速记",
    sold: "1400+",
    price: 39,
    category: "办公文具",
    bg: "linear-gradient(135deg, #ffeef2, #ffd6e0)"
  },
  {
    id: "desk-organizer",
    name: "桌面收纳架",
    subtitle: "整洁有序",
    sold: "2400+",
    price: 89,
    category: "办公文具",
    bg: "linear-gradient(135deg, #dff9fb, #c7ecee)"
  }
];

const hotProductIds = [
  "earbuds-lite",
  "commute-backpack",
  "smart-thermos",
  "mini-coffee-machine"
];

const newProductIds = [
  "light-windbreaker",
  "smart-lamp",
  "candle-set",
  "running-shoes"
];

const withDetail = (product) => {
  if (!product) {
    return null;
  }
  const subtitle = product.subtitle || product.tag || "精选好物";
  const description = product.description
    || `${product.name} 主打 ${subtitle}，适合日常使用与送礼。`;
  const highlights = product.highlights || ["官方正品保障", "支持7天无理由退换", "48小时内发货"];
  return {
    ...product,
    subtitle,
    description,
    highlights
  };
};

const getProductById = (id) => withDetail(productList.find((item) => item.id === id));

const getProductsByIds = (ids) => ids.map((id) => getProductById(id)).filter(Boolean);

// API-backed fetch helpers
import http from "@/api/http";

const fetchProducts = async ({ type, page = 0, size = 20 } = {}) => {
  try {
    const resp = await http.get("/api/products", { params: { type, page, size } });
    const data = resp.data;
    // backend returns a Page object under data (with .content array)
    if (Array.isArray(data)) return data;
    if (data && Array.isArray(data.content)) return data.content;
    return [];
  } catch (e) {
    // fallback to local static
    if (type === "hot") return getProductsByIds(hotProductIds);
    if (type === "new") return getProductsByIds(newProductIds);
    return productList;
  }
};

const fetchProductById = async (id) => {
  try {
    const resp = await http.get(`/api/products/${id}`);
    return resp.data || null;
  } catch (e) {
    return getProductById(id);
  }
};

export {
  productList,
  hotProductIds,
  newProductIds,
  getProductById,
  getProductsByIds,
  fetchProducts,
  fetchProductById
};
