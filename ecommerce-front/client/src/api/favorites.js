const STORAGE_KEY = "eb_favorites";

const getStorageKey = () => {
  const raw = localStorage.getItem("eb_user") || sessionStorage.getItem("eb_user");
  if (!raw) return STORAGE_KEY;
  try {
    const user = JSON.parse(raw);
    return `${STORAGE_KEY}_${user.id}`;
  } catch (e) {
    return STORAGE_KEY;
  }
};

const loadAll = () => {
  try {
    const raw = localStorage.getItem(getStorageKey());
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    return [];
  }
};

const saveAll = (list) => {
  localStorage.setItem(getStorageKey(), JSON.stringify(list));
};

export const getFavorites = () => {
  return loadAll();
};

export const addFavorite = (product) => {
  const list = loadAll();
  if (list.find((p) => p.id === product.id)) return list;
  const item = {
    id: product.id,
    name: product.name,
    price: product.price,
    subtitle: product.subtitle || "",
    image: product.image || product.coverUrl || "",
    category: product.category || "",
    addedAt: Date.now()
  };
  list.unshift(item);
  saveAll(list);
  return list;
};

export const removeFavorite = (productId) => {
  const list = loadAll().filter((p) => p.id !== productId);
  saveAll(list);
  return list;
};

export const isFavorited = (productId) => {
  return loadAll().some((p) => p.id === productId);
};

export const toggleFavorite = (product) => {
  if (isFavorited(product.id)) {
    removeFavorite(product.id);
    return false;
  } else {
    addFavorite(product);
    return true;
  }
};
