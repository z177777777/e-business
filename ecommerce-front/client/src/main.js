import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import "element-plus/dist/index.css";
import "@/styles/base.css";
import App from "./App.vue";
import router from "./router";
import { listAddresses } from "@/api/address";
import { getFavorites } from "@/api/favorites";

async function init() {
	// 在应用启动时强制读取并规范化本地地址（迁移旧数据，确保唯一默认）
	try {
		await listAddresses();
	} catch (e) {
		// ignore
	}
	// 预加载收藏数据，确保 localStorage key 按当前用户正确初始化
	try {
		getFavorites();
	} catch (e) {
		// ignore
	}

	const app = createApp(App);
	app.use(createPinia());
	app.use(router);
	app.use(ElementPlus, { locale: zhCn });
	app.mount("#app");
}

init();
