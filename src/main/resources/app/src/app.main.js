import {createApp, defineAsyncComponent} from "vue";
const App = defineAsyncComponent(() => import("./App.vue"));

import router from "./router/app-router";
import store from "./store/store";
import './assets/main.css'

const Vue = createApp(App);
Vue.config.devtools = process.env.NODE_ENV === "development";
Vue.config.performance = process.env.NODE_ENV === "development";

Vue.use(router);
Vue.use(store);

Vue.mount('.app');