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

Vue.use(Router)

// 路由守卫
function requireSellerAuth(to, from, next) {
  const auth = localStorage.getItem('auth')
  if (auth) {
    next()
  } else {
    next('/seller/login')
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
    }
  ]
})