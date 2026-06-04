import http from "./http";

const TX_PREFIX = "eb_wallet_tx";
const BAL_KEY = "eb_wallet_expected";

const getUserId = () => {
  const raw = localStorage.getItem("eb_user") || sessionStorage.getItem("eb_user");
  if (!raw) return "anonymous";
  try { return JSON.parse(raw).id; } catch (e) { return "anonymous"; }
};

const txKey = () => `${TX_PREFIX}_${getUserId()}`;
const balKey = () => `${BAL_KEY}_${getUserId()}`;

const loadTransactions = () => {
  try {
    const raw = localStorage.getItem(txKey());
    return raw ? JSON.parse(raw) : [];
  } catch (e) { return []; }
};

const saveTransactions = (list) => {
  localStorage.setItem(txKey(), JSON.stringify(list.slice(0, 100)));
};

const getExpectedBalance = () => {
  try { return parseFloat(localStorage.getItem(balKey())) || 0; } catch (e) { return 0; }
};

const setExpectedBalance = (v) => {
  localStorage.setItem(balKey(), Math.round(v * 100) / 100);
};

export const getBalance = async () => {
  try {
    const res = await http.get("/api/wallet");
    return res.data?.balance || 0;
  } catch (e) { return 0; }
};

export const getTransactions = () => loadTransactions();

export const topUp = async (amount) => {
  const res = await http.post(`/api/wallet/top-up?amount=${amount}`);
  const balance = res.data?.balance || 0;
  const tx = { id: Date.now(), type: "充值", amount, balance, time: new Date().toISOString(), orderNo: "" };
  const list = loadTransactions();
  list.unshift(tx);
  saveTransactions(list);
  setExpectedBalance(balance);
  return { balance, tx };
};

export const pay = async (amount, orderNo) => {
  const res = await http.post(`/api/wallet/pay/${amount}`);
  const balance = res.data?.balance || 0;
  const tx = { id: Date.now(), type: "支付", amount: -amount, balance, time: new Date().toISOString(), orderNo };
  const list = loadTransactions();
  list.unshift(tx);
  saveTransactions(list);
  setExpectedBalance(balance);
  return { balance, tx };
};

export const syncRefundTransactions = async () => {
  const serverBal = await getBalance();
  const expected = getExpectedBalance();
  if (expected === 0) {
    setExpectedBalance(serverBal);
    return { balance: serverBal, newTx: null };
  }
  if (serverBal > expected + 0.001) {
    const diff = Math.round((serverBal - expected) * 100) / 100;
    // 查询关联的退款订单
    let orderNo = "";
    try {
      const ordersRes = await http.get("/api/orders");
      const orders = ordersRes.data || [];
      const refunded = orders.find(o => o.status === "REFUNDED" && Math.abs(Number(o.totalAmount) - diff) < 0.01);
      if (refunded) orderNo = refunded.orderNo;
    } catch (e) { /* ignore */ }
    const tx = { id: Date.now(), type: "退款", amount: diff, balance: serverBal, time: new Date().toISOString(), orderNo };
    const list = loadTransactions();
    list.unshift(tx);
    saveTransactions(list);
    setExpectedBalance(serverBal);
    return { balance: serverBal, newTx: tx };
  }
  setExpectedBalance(serverBal);
  return { balance: serverBal, newTx: null };
};
