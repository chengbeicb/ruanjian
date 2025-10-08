<template>
  <v-navigation-drawer
    v-model="drawer"
    app
    clipped
    right
    dark
    permanent
  >
    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="title">
          卖家中心
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ username || '请登录' }}
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    
    <v-divider></v-divider>
    
    <v-list>
      <v-list-item
        v-for="item in menuItems"
        :key="item.title"
        :to="item.route"
        router-link-active="active"
        @click="handleMenuItemClick"
      >
        <v-list-item-icon>
          <v-icon>{{ item.icon }}</v-icon>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
    
    <v-divider class="mt-4"></v-divider>
    
    <v-list class="mt-4">
      <v-list-item @click="handleLogout">
        <v-list-item-icon>
          <v-icon>mdi-logout</v-icon>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title>退出登录</v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  name: 'SellerNavigation',
  data() {
    return {
      drawer: true,
      username: localStorage.getItem('sellerUsername')
    }
  },
  computed: {
    menuItems() {
      return [
        {
          title: '仪表盘',
          route: '/seller/dashboard',
          icon: 'mdi-view-dashboard'
        },
        {
          title: '商品管理',
          route: '/seller/products',
          icon: 'mdi-shopping-bag'
        },
        {
          title: '订单管理',
          route: '/seller/orders',
          icon: 'mdi-file-document-multiple'
        },
        {
          title: '修改密码',
          route: '/seller/change-password',
          icon: 'mdi-lock'
        }
      ]
    }
  },
  methods: {
    handleMenuItemClick() {
      // 在移动设备上，点击菜单项后关闭抽屉
      if (this.$vuetify.breakpoint.xsOnly) {
        this.drawer = false
      }
    },
    handleLogout() {
      if (confirm('确定要退出登录吗？')) {
        // 清除本地存储的认证信息
        localStorage.removeItem('sellerAuth')
        localStorage.removeItem('sellerUsername')
        // 跳转到登录页面
        this.$router.push('/seller/login')
        // 显示退出成功提示
        this.$snackbar.open({
          message: '已成功退出登录',
          type: 'success'
        })
      }
    }
  }
}
</script>

<style scoped>
.v-navigation-drawer {
  background-color: #1976d2;
}

.active {
  background-color: rgba(255, 255, 255, 0.15);
}

.title {
  font-size: 1.2rem;
}
</style>