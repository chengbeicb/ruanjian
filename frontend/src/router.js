import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import ProductDetail from './views/ProductDetail.vue'
import PurchaseForm from './views/PurchaseForm.vue'
import SellerLogin from './views/SellerLogin.vue'
import SellerDashboard from './views/SellerDashboard.vue'
import ProductManagement from './views/ProductManagement.vue'
import OrderManagement from './views/OrderManagement.vue'
import ChangePassword from './views/ChangePassword.vue'
import CustomerRegister from './views/CustomerRegister.vue'
import CustomerLogin from './views/CustomerLogin.vue'
import CustomerOrderHistory from './views/CustomerOrderHistory.vue'
import CustomerManagement from './views/CustomerManagement.vue'

Vue.use(Router)

// 路由守卫 - 卖家认证
function requireSellerAuth(to, from, next) {
  if (Vue.prototype.$validateAuth && Vue.prototype.$validateAuth()) {
    next()
  } else {
    localStorage.removeItem('auth')
    next('/seller/login')
  }
}

// 路由守卫 - 客户认证
function requireCustomerAuth(to, from, next) {
  if (Vue.prototype.$validateCustomerAuth && Vue.prototype.$validateCustomerAuth()) {
    next()
  } else {
    localStorage.removeItem('auth')
    localStorage.removeItem('customerUsername')
    next('/customer/login')
  }
}

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: ProductDetail,
      props: true
    },
    {
      path: '/product/:id/purchase',
      name: 'purchase-form',
      component: PurchaseForm,
      props: true
    },
    // 卖家相关路由
    {
      path: '/seller/login',
      name: 'seller-login',
      component: SellerLogin
    },
    {
      path: '/seller/dashboard',
      name: 'seller-dashboard',
      component: SellerDashboard,
      beforeEnter: requireSellerAuth
    },
    {
      path: '/seller/products',
      name: 'product-management',
      component: ProductManagement,
      beforeEnter: requireSellerAuth
    },
    {
      path: '/seller/orders',
      name: 'order-management',
      component: OrderManagement,
      beforeEnter: requireSellerAuth
    },
    {
      path: '/seller/password',
      name: 'change-password',
      component: ChangePassword,
      beforeEnter: requireSellerAuth
    },
    {
      path: '/seller/customers',
      name: 'customer-management',
      component: CustomerManagement,
      beforeEnter: requireSellerAuth
    },
    // 客户相关路由
    {
      path: '/customer/register',
      name: 'customer-register',
      component: CustomerRegister
    },
    {
      path: '/customer/login',
      name: 'customer-login',
      component: CustomerLogin
    },
    {
      path: '/customer/orders',
      name: 'customer-order-history',
      component: CustomerOrderHistory,
      beforeEnter: requireCustomerAuth
    }
  ]
})