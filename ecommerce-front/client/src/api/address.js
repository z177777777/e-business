// 简单的本地存储地址 API（按账号隔离）。生产应替换为实际后端接口
const STORAGE_KEY = "addresses";
const USER_KEY = "eb_user";

const loadUser = () => {
  const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
};

const storageKeyForCurrent = () => {
  const user = loadUser();
  return user && user.id ? `${STORAGE_KEY}_${user.id}` : STORAGE_KEY;
};

const readAll = () => {
  try {
    const key = storageKeyForCurrent();
    const raw = localStorage.getItem(key) || "[]";
    return JSON.parse(raw);
  } catch (e) {
    return [];
  }
};

const writeAll = (list) => {
  const key = storageKeyForCurrent();
  localStorage.setItem(key, JSON.stringify(list));
};

const genId = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
const normalizeAddresses = (list) => {
  const arr = Array.isArray(list) ? list : [];
  // 强制为布尔，并确保最多只有一个默认地址（保留第一个为真的）
  let foundDefault = false;
  for (let i = 0; i < arr.length; i++) {
    const item = arr[i];
    item.isDefault = !!item.isDefault;
    if (item.isDefault && !foundDefault) {
      foundDefault = true;
      continue;
    }
    // 若已经找到默认或当前不标记为默认，则清除标记
    item.isDefault = false;
  }
  if (!foundDefault && arr.length > 0) {
    arr[0].isDefault = true;
  }
  return arr;
};

export const listAddresses = async () => {
  const list = normalizeAddresses(readAll());
  writeAll(list);
  return Promise.resolve({ data: list });
};

export const createAddress = async (payload) => {
  const list = normalizeAddresses(readAll());
  const item = Object.assign({ id: genId(), isDefault: list.length === 0 }, payload);
  list.push(item);
  const normalized = normalizeAddresses(list);
  writeAll(normalized);
  return Promise.resolve({ data: item });
};

export const updateAddress = async (id, payload) => {
  const list = normalizeAddresses(readAll());
  const idx = list.findIndex((a) => a.id === id);
  if (idx === -1) return Promise.reject(new Error("not found"));
  list[idx] = Object.assign({}, list[idx], payload);
  const normalized = normalizeAddresses(list);
  writeAll(normalized);
  return Promise.resolve({ data: list[idx] });
};

export const deleteAddress = async (id) => {
  let list = normalizeAddresses(readAll());
  list = list.filter((a) => a.id !== id);
  const normalized = normalizeAddresses(list);
  writeAll(normalized);
  return Promise.resolve({ data: true });
};

export const setDefaultAddress = async (id) => {
  const list = normalizeAddresses(readAll());
  const sid = String(id);
  list.forEach((a) => (a.isDefault = String(a.id) === sid));
  const normalized = normalizeAddresses(list);
  writeAll(normalized);
  return Promise.resolve({ data: true });
};

export default {
  listAddresses,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
};
