import { defineStore } from "pinia";

const TOKEN_KEY = "eb_token";
const USER_KEY = "eb_user";
const REMEMBER_EMAIL_KEY = "eb_remember_email";

const loadToken = () => {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || "";
};

const loadUser = () => {
  const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: loadToken(),
    user: loadUser(),
    rememberedEmail: localStorage.getItem(REMEMBER_EMAIL_KEY) || ""
  }),
  actions: {
    setSession(token, user, rememberMe) {
      this.token = token;
      this.user = user;
      if (rememberMe) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(USER_KEY);
      } else {
        sessionStorage.setItem(TOKEN_KEY, token);
        sessionStorage.setItem(USER_KEY, JSON.stringify(user));
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
      }
    },
    setRememberedEmail(email) {
      this.rememberedEmail = email || "";
      if (email) {
        localStorage.setItem(REMEMBER_EMAIL_KEY, email);
      } else {
        localStorage.removeItem(REMEMBER_EMAIL_KEY);
      }
    },
    clearSession() {
      this.token = "";
      this.user = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
    },
    updateUser(user) {
      this.user = user;
      if (localStorage.getItem(TOKEN_KEY)) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
      } else if (sessionStorage.getItem(TOKEN_KEY)) {
        sessionStorage.setItem(USER_KEY, JSON.stringify(user));
      }
    }
  }
});
