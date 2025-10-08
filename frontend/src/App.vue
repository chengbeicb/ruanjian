<template>
  <v-app>
    <v-toolbar color="primary" dark>
      <v-toolbar-title>简单在线购物系统</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn to="/" text>首页</v-btn>
        <v-btn to="/seller/login" text>卖家登录</v-btn>
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