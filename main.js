import App from './App.vue'
import router from './router'
import Vuetify from 'vuetify'
import 'vuetify/dist/vuetify.min.css'
import axios from 'axios'

// 配置axios基础URL
axios.defaults.baseURL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

// 添加请求拦截器，处理认证
axios.interceptors.request.use(config => {
    const auth = localStorage.getItem('auth')
    if (auth) {
        config.headers.Authorization = `Basic ${auth}`
    }
    return config
})

// 添加响应拦截器，处理认证错误
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('auth')
            // 检查当前是否已经在登录页面，避免重复导航
            if (router.currentRoute.path !== '/seller/login') {
                router.push('/seller/login')
            }
        }
        return Promise.reject(error)
    }
)

// 关键修复：添加缺失的$validateAuth方法
Vue.prototype.$validateAuth = function() {
    const auth = localStorage.getItem('auth')
    if (!auth) {
        return false
    }
    // 验证auth格式是否正确
    try {
        const decoded = atob(auth)
        return decoded.includes(':')
    } catch (e) {
        return false
    }
}

Vue.use(Vuetify)

Vue.prototype.$http = axios

Vue.config.productionTip = false

new Vue({
    router,
    vuetify: new Vuetify({
        theme: {
            themes: {
                light: {
                    primary: '#1976D2',
                    secondary: '#424242',
                    accent: '#82B1FF',
                    error: '#FF5252',
                    info: '#2196F3',
                    success: '#4CAF50',
                    warning: '#FFC107'
                }
            }
        }
    }),
    render: h => h(App)
}).$mount('#app')