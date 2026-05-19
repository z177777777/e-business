import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const sourcePath = path.resolve(__dirname, "../ecommerce-front/client/src/data/products.js");
const outPath = path.resolve(__dirname, "seed_products.sql");

const source = fs.readFileSync(sourcePath, "utf8");

function extractArray(name) {
  const re = new RegExp(`const\\s+${name}\\s*=\\s*(\\[[\\s\\S]*?\\]);`);
  const match = source.match(re);
  if (!match) {
    throw new Error(`Cannot find array: ${name}`);
  }
  return Function(`\"use strict\"; return (${match[1]});`)();
}

function escapeSql(value) {
  if (value === null || value === undefined) {
    return "NULL";
  }
  return `'${String(value).replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

function toSoldCount(value) {
  if (typeof value === "number") {
    return Math.max(0, Math.floor(value));
  }
  const text = String(value || "").trim().toLowerCase();
  if (!text) {
    return 0;
  }
  if (text.includes("w")) {
    const numeric = Number.parseFloat(text.replace(/[^0-9.]/g, ""));
    return Number.isFinite(numeric) ? Math.floor(numeric * 10000) : 0;
  }
  const numeric = Number.parseInt(text.replace(/[^0-9]/g, ""), 10);
  return Number.isFinite(numeric) ? numeric : 0;
}

const productList = extractArray("productList");
const hotProductIds = new Set(extractArray("hotProductIds"));
const newProductIds = new Set(extractArray("newProductIds"));

const header = `USE ecommerce;\n\nTRUNCATE TABLE products;\n\nINSERT INTO products\n(name, subtitle, description, price, sold_count, cover_url, category, is_hot, is_new)\nVALUES\n`;

const values = productList.map((p) => {
  const description = `${p.name} 主打 ${p.subtitle || p.tag || "精选好物"}，适合日常使用与送礼。`;
  const soldCount = toSoldCount(p.sold);
  const isHot = hotProductIds.has(p.id) ? 1 : 0;
  const isNew = newProductIds.has(p.id) ? 1 : 0;
  return `(${escapeSql(p.name)}, ${escapeSql(p.subtitle || null)}, ${escapeSql(description)}, ${Number(p.price || 0)}, ${soldCount}, ${escapeSql(p.bg || null)}, ${escapeSql(p.category || null)}, ${isHot}, ${isNew})`;
});

const sql = `${header}${values.join(",\n")};\n`;
fs.writeFileSync(outPath, sql, "utf8");

console.log(`Generated SQL with ${productList.length} products: ${outPath}`);
