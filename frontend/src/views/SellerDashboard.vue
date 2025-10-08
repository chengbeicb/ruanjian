<template>
  <div class="seller-dashboard">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>卖家控制面板</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/seller/products" text>商品管理</v-btn>
          <v-btn to="/seller/orders" text>订单管理</v-btn>
          <v-btn to="/seller/password" text>修改密码</v-btn>
          <v-btn @click="logout" text>退出登录</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>仪表盘</h3>
            </v-card-title>
            <v-card-text>
              <v-layout wrap>
                <v-flex xs12 md4>
                  <v-card color="primary" dark class="dashboard-card">
                    <v-card-text class="text-center">
                      <h4>当前商品</h4>
                      <p class="dashboard-number">{{ productCount }}</p>
                    </v-card-text>
                  </v-card>
                </v-flex>
                <v-flex xs12 md4>
                  <v-card color="success" dark class="dashboard-card">
                    <v-card-text class="text-center">
                      <h4>待处理订单</h4>
                      <p class="dashboard-number">{{ pendingOrderCount }}</p>
                    </v-card-text>
                  </v-card>
                </v-flex>
                <v-flex xs12 md4>
                  <v-card color="info" dark class="dashboard-card">
                    <v-card-text class="text-center">
                      <h4>已完成订单</h4>
                      <p class="dashboard-number">{{ completedOrderCount }}</p>
                    </v-card-text>
                  </v-card>
                </v-flex>
              </v-layout>
              <v-divider class="my-6"></v-divider>
              <v-layout>
                <v-flex xs12>
                  <v-card>
                    <v-card-title>操作指南</v-card-title>
                    <v-card-text>
                      <ol>
                        <li>在商品管理中发布您的商品</li>
                        <li>当有买家表达购买意向时，在订单管理中查看并联系买家</li>
                        <li>完成线下交易后，在订单管理中将订单标记为完成</li>
                        <li>如需修改密码，请前往修改密码页面</li>
                      </ol>
                    </v-card-text>
                  </v-card>
                </v-flex>
              </v-layout>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'SellerDashboard',
  data() {
    return {
      productCount: 0,
      pendingOrderCount: 0,
      completedOrderCount: 0
    }
  },
  mounted() {
    this.fetchDashboardData()
  },
  methods: {
    fetchDashboardData() {
      // 获取卖家商品数量
      this.$http.get('/products/seller')
        .then(response => {
          this.productCount = response.data.length
        })
        .catch(error => {
          console.error('获取商品数量失败:', error)
        })
      
      // 获取订单统计
      this.$http.get('/purchase-intents')
        .then(response => {
          const intents = response.data
          this.pendingOrderCount = intents.filter(intent => !intent.completed && !intent.canceled).length
          this.completedOrderCount = intents.filter(intent => intent.completed).length
        })
        .catch(error => {
          console.error('获取订单统计失败:', error)
        })
    },
    logout() {
      localStorage.removeItem('auth')
      this.$router.push('/seller/login')
    }
  }
}
</script>

<style scoped>
.seller-dashboard {
  padding: 20px 0;
}
.dashboard-card {
  height: 100%;
  transition: transform 0.3s ease;
}
.dashboard-card:hover {
  transform: translateY(-5px);
}
.dashboard-number {
  font-size: 2.5rem;
  font-weight: bold;
  margin: 0;
}
</style>