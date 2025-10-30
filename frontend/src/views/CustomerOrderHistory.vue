<template>
  <div class="customer-order-history">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>我的订单</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn @click="logout" color="secondary" text>退出登录</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>历史订单记录</h3>
            </v-card-title>
            <v-card-text>
              <v-container v-if="orders.length > 0">
                <v-layout wrap>
                  <v-flex xs12 v-for="order in orders" :key="order.id">
                    <v-card hover class="mb-4">
                      <v-layout row>
                        <v-flex xs3>
                          <v-img
                            :src="getFirstImageUrl(order.product.imageUrls)"
                            aspect-ratio="1/1"
                            class="grey lighten-2"
                          ></v-img>
                        </v-flex>
                        <v-flex xs9>
                          <v-card-title>{{ order.product.name }}</v-card-title>
                          <v-card-text>
                            <div class="mb-2">
                              <v-chip :color="getOrderStatusColor(order)" text-color="white" class="mr-2">
                                {{ getOrderStatusText(order) }}
                              </v-chip>
                              <span class="text-body-2">订单号: {{ order.id }}</span>
                            </div>
                            <div class="mb-2">
                              <span class="font-weight-bold">价格: </span>
                              <span class="text-primary">¥{{ order.product.price.toFixed(2) }}</span>
                            </div>
                            <div class="mb-2">
                              <span class="font-weight-bold">下单时间: </span>
                              <span>{{ formatDateTime(order.createTime) }}</span>
                            </div>
                            <div v-if="order.completeTime">
                              <span class="font-weight-bold">完成时间: </span>
                              <span>{{ formatDateTime(order.completeTime) }}</span>
                            </div>
                          </v-card-text>
                          <v-card-actions>
                            <v-btn @click="viewProduct(order.product.id)" color="primary">查看商品</v-btn>
                          </v-card-actions>
                        </v-flex>
                      </v-layout>
                    </v-card>
                  </v-flex>
                </v-layout>
              </v-container>
              <v-container v-else>
                <v-alert type="info" dismissible>
                  暂无订单记录
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.type === 'error' ? 'error' : 'success'"
      timeout="6000"
    >
      {{ snackbar.message }}
      <v-btn color="accent" text @click="snackbar.show = false">
        关闭
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'CustomerOrderHistory',
  data() {
    return {
      orders: [],
      loading: false,
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  beforeMount() {
    // 检查登录状态
    if (!this.$validateCustomerAuth()) {
      this.$router.push('/customer/login')
    }
  },
  mounted() {
    this.fetchOrders()
  },
  methods: {
    fetchOrders() {
      this.loading = true
      this.$http.get('/customer/orders')
        .then(response => {
          this.loading = false
          this.orders = response.data
        })
        .catch(error => {
          this.loading = false
          this.snackbar = {
            show: true,
            message: error.response?.data || '获取订单失败',
            type: 'error'
          }
        })
    },
    getFirstImageUrl(imageUrls) {
      if (!imageUrls) return 'https://via.placeholder.com/400x400?text=暂无图片'
      
      const firstUrl = imageUrls.split(',')[0]
      // 处理URL格式
      if (firstUrl.startsWith('http')) {
        return firstUrl
      } else if (firstUrl.startsWith('/uploads/')) {
        return 'http://localhost:8080' + firstUrl
      } else {
        return 'http://localhost:8080/picture/' + firstUrl
      }
    },
    getOrderStatusColor(order) {
      if (order.completed) return 'success'
      if (order.canceled) return 'error'
      return 'warning'
    },
    getOrderStatusText(order) {
      if (order.completed) return '已完成'
      if (order.canceled) return '已取消'
      return '处理中'
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    },
    viewProduct(productId) {
      this.$router.push({ name: 'product-detail', params: { id: productId } })
    },
    logout() {
      localStorage.removeItem('auth')
      localStorage.removeItem('customerUsername')
      this.$router.push('/customer/login')
    }
  }
}
</script>

<style scoped>
.customer-order-history {
  padding: 20px 0;
}
</style>