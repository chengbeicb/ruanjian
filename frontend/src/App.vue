<template>
  <v-app>
    <v-toolbar color="primary" dark>
      <v-toolbar-title>简单在线购物系统</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn to="/" text>首页</v-btn>
        
        <!-- 客户相关导航 -->
        <template v-if="!isCustomerLoggedIn">
          <v-btn to="/customer/register" text>客户注册</v-btn>
          <v-btn to="/customer/login" text>客户登录</v-btn>
        </template>
        <template v-else>
          <v-btn to="/customer/orders" text>我的订单</v-btn>
          <v-btn @click="logoutCustomer" text>客户退出</v-btn>
        </template>
        
        <!-- 卖家相关导航 -->
        <template v-if="!isSellerLoggedIn">
          <v-btn to="/seller/login" text>卖家登录</v-btn>
        </template>
        <template v-else>
          <v-btn to="/seller/dashboard" text>卖家中心</v-btn>
          <v-btn to="/seller/products" text>商品管理</v-btn>
          <v-btn to="/seller/orders" text>订单管理</v-btn>
          <v-btn to="/seller/customers" text>客户管理</v-btn>
          <v-btn @click="logoutSeller" text>卖家退出</v-btn>
        </template>
      </v-toolbar-items>
    </v-toolbar>
    <v-main>
      <router-view></router-view>
    </v-main>
    <v-footer color="primary" dark>
      <div class="container">
        <div class="text-center py-4">
          <span>&copy; {{ new Date().getFullYear() }} 简单在线购物系统</span>
        </div>
      </div>
    </v-footer>
  </v-app>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      //
    }
  },
  computed: {
    // 检查客户是否已登录
    isCustomerLoggedIn() {
      return this.$validateCustomerAuth ? this.$validateCustomerAuth() : false
    },
    // 检查卖家是否已登录
    isSellerLoggedIn() {
      return this.$validateAuth ? this.$validateAuth() : false
    }
  },
  methods: {
    // 客户退出登录
    logoutCustomer() {
      localStorage.removeItem('customerUsername')
      // 刷新页面以更新导航状态
      location.reload()
    },
    // 卖家退出登录
    logoutSeller() {
      localStorage.removeItem('auth')
      // 刷新页面以更新导航状态
      location.reload()
    }
  }
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Roboto', sans-serif;
}
</style>