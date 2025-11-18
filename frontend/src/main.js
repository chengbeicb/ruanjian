import Vue from 'vue'
import App from './App.vue'
import router from './router'
import Vuetify from 'vuetify'
import 'vuetify/dist/vuetify.min.css'
import axios from 'axios'

// 使用同源相对路径，方便前端被后端静态托管时自动匹配端口。
// 开发模式若前端独立运行（如: npm run serve 开在 8081/8082），请设置环境变量 VUE_APP_API_BASE_URL 指向后端地址，如 http://localhost:8080
axios.defaults.baseURL = process.env.VUE_APP_API_BASE_URL || ''
axios.defaults.withCredentials = true // 若后端使用 Cookie 会话或需要跨域携带凭证

// 添加请求拦截器，处理认证
axios.interceptors.request.use(config => {
  const authToken = localStorage.getItem('authToken')
  if (authToken) {
    // 确认 authToken 形式是否已经是 btoa('user:pass'); 如果后端不是 Basic，可调整为 Bearer
    config.headers.Authorization = `Basic ${authToken}`
  }
  // 可选调试输出
  // console.debug('[REQUEST]', config.method, config.url)
  return config
})

// 保留多候选登录与后续的 attemptLogin / applyServerToken 版本
// 多候选路径(前端自动重试，首个成功即返回)
const CUSTOMER_LOGIN_CANDIDATES = [
  process.env.VUE_APP_CUSTOMER_LOGIN_PATH || '/api/customer/login',
  '/api/customer/auth/login'
]
const SELLER_LOGIN_CANDIDATES = [
  process.env.VUE_APP_SELLER_LOGIN_PATH || '/api/seller/login',
  '/api/seller/auth/login'
]

// 自动应用后端返回 token: 若返回 {token: 'xxx'} => 使用 Bearer
function applyServerToken(resData, username, password, role) {
  if (resData && resData.token) {
    localStorage.setItem('authToken', resData.token)
    localStorage.setItem('auth', role)
    if (role === 'customer') localStorage.setItem('customerUsername', username)
    if (role === 'seller') localStorage.setItem('sellerUsername', username)
    // 转为 Bearer 模式
    axios.interceptors.request.use(cfg => {
      const tk = localStorage.getItem('authToken')
      if (tk) cfg.headers.Authorization = `Bearer ${tk}`
      return cfg
    })
  } else {
    // 回退 Basic
    Vue.prototype.$setAuth({ username, password, role })
  }
}

// 统一重试函数
async function attemptLogin(paths, payload) {
  const errors = []
  for (const p of paths) {
    try {
      const res = await axios.post(p, payload)
      return { data: res.data, path: p }
    } catch (e) {
      errors.push({ path: p, status: e.response && e.response.status })
      if (e.response && e.response.status >= 500) break
    }
  }
  throw new Error('登录路径全部失败: ' + JSON.stringify(errors))
}

// 客户登录
Vue.prototype.$loginCustomer = async function(username, password) {
  if (!username || !password) throw new Error('缺少用户名或密码')
  const { data, path } = await attemptLogin(CUSTOMER_LOGIN_CANDIDATES, { username, password }).catch(e => {
    console.error('[CUSTOMER LOGIN FAIL]', e.message)
    throw e
  })
  applyServerToken(data, username, password, 'customer')
  console.info('[CUSTOMER LOGIN OK]', path)
  router.replace('/customer/dashboard')
  return data
}

// 商家登录
Vue.prototype.$loginSeller = async function(username, password) {
  if (!username || !password) throw new Error('缺少用户名或密码')
  const { data, path } = await attemptLogin(SELLER_LOGIN_CANDIDATES, { username, password }).catch(e => {
    console.error('[SELLER LOGIN FAIL]', e.message)
    throw e
  })
  applyServerToken(data, username, password, 'seller')
  console.info('[SELLER LOGIN OK]', path)
  router.replace('/seller/dashboard')
  return data
}

// 新增注销
Vue.prototype.$logout = function() {
  localStorage.removeItem('auth')
  localStorage.removeItem('authToken')
  localStorage.removeItem('customerUsername')
  localStorage.removeItem('sellerUsername')
  router.replace('/customer/login')
}

// 添加响应拦截器，处理认证错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      console.warn('[AUTH 401] 清除本地认证并重定向', error.response.data)
      localStorage.removeItem('auth')
      localStorage.removeItem('authToken')
      localStorage.removeItem('customerUsername')
      localStorage.removeItem('sellerUsername')
      const currentPath = router.currentRoute.path
      // 扩展: 若既不是 seller 也不是 customer 的专属路径，默认跳转到客户登录或统一登录页
      if (!currentPath.startsWith('/seller/login') && !currentPath.startsWith('/customer/login')) {
        if (currentPath.startsWith('/seller/')) {
          router.push('/seller/login')
        } else if (currentPath.startsWith('/customer/')) {
          router.push('/customer/login')
        } else {
          // 可根据项目实际选择一个默认登录页
          router.push('/customer/login')
        }
      }
    } else if (error.response) {
      if (error.response.status === 404) {
        console.warn('[API 404] 找不到接口:', error.config && error.config.method, error.config && error.config.url)
      } else if (error.response.status === 405) {
        console.warn('[API 405] 方法不被允许，请核对请求动词与后端实现:', error.config && error.config.url)
      }
    }
    return Promise.reject(error)
  }
)

// 统一认证验证方法(基础令牌格式校验)
Vue.prototype.$validateAuth = function() {
  const auth = localStorage.getItem('auth')            // 角色: seller / customer
  const authToken = localStorage.getItem('authToken')  // 期望为 base64(user:pass)
  if (!auth || !authToken) return false
  try {
    const decoded = atob(authToken)
    // Basic 规范: "username:password"
    return decoded.includes(':') && decoded.split(':')[0].length > 0
  } catch (e) {
    console.warn('[AUTH TOKEN DECODE FAILED]', e)
    return false
  }
}

// 卖家认证验证方法(基于角色 + 令牌)
Vue.prototype.$validateSellerAuth = function() {
  return localStorage.getItem('auth') === 'seller' && this.$validateAuth()
}

// 客户认证验证方法(基于角色 + 令牌 + 可选用户名存在性)
Vue.prototype.$validateCustomerAuth = function() {
  const ok = localStorage.getItem('auth') === 'customer' && this.$validateAuth()
  if (!ok) return false
  // 如果后端需要用户名，可一起校验
  const customerUsername = localStorage.getItem('customerUsername')
  return !!customerUsername
}

// 新增: 统一设置认证数据工具方法，登录成功后直接调用
Vue.prototype.$setAuth = function({ username, password, role }) {
  if (!username || !password || !role) return
  localStorage.setItem('authToken', btoa(username + ':' + password))
  localStorage.setItem('auth', role)
  if (role === 'customer') localStorage.setItem('customerUsername', username)
  if (role === 'seller') localStorage.setItem('sellerUsername', username)
  // 登录后跳转统一使用 router.replace 避免历史堆积 & 减少重复导航
  // 示例: if (role === 'seller') router.replace('/seller/dashboard')
}

// 包装 push/replace，忽略重复与守卫重定向产生的 Promise 拒绝日志
const wrapNav = fn => function(location, onResolve, onReject) {
  const p = fn.call(this, location, onResolve, onReject)
  if (p && p.catch) {
    return p.catch(err => {
      if (!err) return
      const msg = err.message || ''
      if (
        err.name === 'NavigationDuplicated' ||
        /Redirected when going from/.test(msg) ||
        /avoided redundant navigation/.test(msg)
      ) {
        // 静默忽略
        return
      }
      throw err
    })
  }
  return p
}
router.push = wrapNav(router.push)
router.replace = wrapNav(router.replace)

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