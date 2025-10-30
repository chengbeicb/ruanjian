import Vue from 'vue'
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
      // 清除认证信息
      localStorage.removeItem('auth')
      localStorage.removeItem('customerUsername')
      
      // 根据当前路由判断重定向到对应的登录页面
      const currentPath = router.currentRoute.path
      if (!currentPath.startsWith('/seller/login') && !currentPath.startsWith('/customer/login')) {
        // 如果是卖家相关路由，重定向到卖家登录
        if (currentPath.startsWith('/seller/')) {
          router.push('/seller/login')
        } else if (currentPath.startsWith('/customer/')) {
          // 如果是客户相关路由，重定向到客户登录
          router.push('/customer/login')
        }
      }
    }
    return Promise.reject(error)
  }
)

// 卖家认证验证方法
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

// 客户认证验证方法
Vue.prototype.$validateCustomerAuth = function() {
  const customerUsername = localStorage.getItem('customerUsername')
  // 简单检查是否存在客户用户名
  return !!customerUsername
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