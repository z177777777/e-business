// 简单的本地存储地址 API，生产应替换为实际后端接口
const STORAGE_KEY = "demo_addresses";

const readAll = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY) || "[]";
    return JSON.parse(raw);
  } catch (e) {
    return [];
  }
};

const writeAll = (list) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
};

const genId = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 8);

export const listAddresses = async () => {
  return Promise.resolve({ data: readAll() });
};

export const createAddress = async (payload) => {
  const list = readAll();
  const item = Object.assign({ id: genId(), isDefault: list.length === 0 }, payload);
  list.push(item);
  writeAll(list);
  return Promise.resolve({ data: item });
};

export const updateAddress = async (id, payload) => {
  const list = readAll();
  const idx = list.findIndex((a) => a.id === id);
  if (idx === -1) return Promise.reject(new Error("not found"));
  list[idx] = Object.assign({}, list[idx], payload);
  writeAll(list);
  return Promise.resolve({ data: list[idx] });
};

export const deleteAddress = async (id) => {
  let list = readAll();
  list = list.filter((a) => a.id !== id);
  // 如果删除后没有默认地址，设置第一个为默认
  if (!list.some((a) => a.isDefault) && list.length > 0) {
    list[0].isDefault = true;
  }
  writeAll(list);
  return Promise.resolve({ data: true });
};

export const setDefaultAddress = async (id) => {
  const list = readAll();
  list.forEach((a) => (a.isDefault = a.id === id));
  writeAll(list);
  return Promise.resolve({ data: true });
};

export default {
  listAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
};
