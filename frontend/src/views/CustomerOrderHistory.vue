<template>
  <div class="customer-order-history">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>我的订单</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/" text>返回首页</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <!-- 订单筛选标签 -->
      <v-tabs v-model="activeTab" class="mt-4">
        <v-tab>全部</v-tab>
        <v-tab>待确认</v-tab>
        <v-tab>已确认</v-tab>
        <v-tab>备货中</v-tab>
        <v-tab>已发货</v-tab>
        <v-tab>已完成</v-tab>
        <v-tab>已取消</v-tab>
      </v-tabs>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>订单列表</h3>
              <v-spacer></v-spacer>
              <v-btn color="primary" text @click="fetchOrders" :loading="loading">
                <v-icon left>mdi-refresh</v-icon>
                刷新
              </v-btn>
            </v-card-title>
            <v-card-text>
              <v-container v-if="loading">
                <v-row justify="center">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </v-row>
              </v-container>
              
              <v-container v-else-if="filteredOrders.length > 0">
                <v-card 
                  v-for="order in filteredOrders" 
                  :key="order.id" 
                  class="mb-4 order-card"
                  outlined
                >
                  <!-- 订单头部 -->
                  <v-card-title class="py-2 grey lighten-4">
                    <div class="d-flex align-center flex-wrap" style="width: 100%">
                      <span class="text-body-2 mr-4">
                        <strong>订单号:</strong> {{ order.orderNumber }}
                      </span>
                      <span class="text-body-2 mr-4">
                        <strong>下单时间:</strong> {{ formatDate(order.createTime) }}
                      </span>
                      <v-spacer></v-spacer>
                      <v-chip 
                        :color="getStatusColor(order.status)" 
                        text-color="white"
                        small
                      >
                        {{ getStatusText(order.status) }}
                      </v-chip>
                    </div>
                  </v-card-title>
                  
                  <v-divider></v-divider>
                  
                  <!-- 商品列表 -->
                  <v-card-text class="py-2">
                    <v-row 
                      v-for="item in order.orderItems" 
                      :key="item.id"
                      class="order-item py-2"
                      align="center"
                    >
                      <v-col cols="2" sm="1">
                        <v-img
                          :src="getImageUrl(item.product)"
                          aspect-ratio="1"
                          max-width="60"
                          max-height="60"
                          class="grey lighten-2"
                          contain
                        ></v-img>
                      </v-col>
                      <v-col cols="5" sm="6">
                        <div class="font-weight-medium">{{ item.product.name }}</div>
                        <div class="text-caption grey--text">
                          单价: ¥{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }}
                        </div>
                      </v-col>
                      <v-col cols="3" sm="3" class="text-right">
                        <span class="red--text font-weight-bold">
                          ¥{{ item.subtotal.toFixed(2) }}
                        </span>
                      </v-col>
                      <v-col cols="2" class="text-right">
                        <v-btn 
                          x-small 
                          color="primary" 
                          text
                          @click="viewProduct(item.product.id)"
                        >
                          查看商品
                        </v-btn>
                      </v-col>
                    </v-row>
                  </v-card-text>
                  
                  <v-divider></v-divider>
                  
                  <!-- 订单底部 -->
                  <v-card-actions class="py-2 px-4">
                    <div class="text-body-2">
                      <span class="mr-4">
                        <strong>收货人:</strong> {{ order.receiverName }}
                      </span>
                      <span class="mr-4">
                        <strong>电话:</strong> {{ order.receiverPhone }}
                      </span>
                    </div>
                    <v-spacer></v-spacer>
                    <span class="text-h6 mr-4">
                      共 {{ getTotalQuantity(order) }} 件，合计: 
                      <strong class="red--text">¥{{ order.totalAmount.toFixed(2) }}</strong>
                    </span>
                    
                    <!-- 操作按钮 -->
                    <v-btn
                      v-if="canCancel(order)"
                      color="error"
                      small
                      outlined
                      @click="showCancelDialog(order)"
                    >
                      取消订单
                    </v-btn>
                    <v-btn
                      color="primary"
                      small
                      @click="showOrderDetail(order)"
                    >
                      订单详情
                    </v-btn>
                  </v-card-actions>
                  
                  <!-- 取消信息 -->
                  <div v-if="order.status === 'CANCELLED'" class="px-4 pb-3">
                    <v-alert type="error" dense text class="mb-0">
                      <strong>取消原因:</strong> {{ order.cancelReason }}
                      <span class="ml-4">
                        <strong>取消方:</strong> {{ order.cancelRole === 'CUSTOMER' ? '您主动取消' : '商家取消' }}
                      </span>
                      <span class="ml-4">
                        <strong>取消时间:</strong> {{ formatDate(order.cancelTime) }}
                      </span>
                    </v-alert>
                  </div>
                </v-card>
              </v-container>
              
              <v-container v-else>
                <v-alert type="info">
                  暂无订单记录
                  <template v-slot:append>
                    <v-btn to="/" color="primary" text>去购物</v-btn>
                  </template>
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
      
      <!-- 订单详情对话框 -->
      <v-dialog v-model="detailDialog.show" max-width="700px">
        <v-card v-if="detailDialog.order">
          <v-card-title class="primary white--text">
            订单详情
          </v-card-title>
          <v-card-text class="pt-4">
            <v-list dense>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单编号</v-list-item-title>
                  <v-list-item-subtitle>{{ detailDialog.order.orderNumber }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单状态</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip 
                      :color="getStatusColor(detailDialog.order.status)" 
                      small 
                      text-color="white"
                    >
                      {{ getStatusText(detailDialog.order.status) }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>收货人</v-list-item-title>
                  <v-list-item-subtitle>{{ detailDialog.order.receiverName }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>联系电话</v-list-item-title>
                  <v-list-item-subtitle>{{ detailDialog.order.receiverPhone }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>收货地址</v-list-item-title>
                  <v-list-item-subtitle>{{ detailDialog.order.shippingAddress }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item v-if="detailDialog.order.remark">
                <v-list-item-content>
                  <v-list-item-title>订单备注</v-list-item-title>
                  <v-list-item-subtitle>{{ detailDialog.order.remark }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>下单时间</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(detailDialog.order.createTime) }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              
              <v-divider class="my-3"></v-divider>
              <v-subheader>商品明细</v-subheader>
              
              <v-list-item v-for="item in detailDialog.order.orderItems" :key="item.id">
                <v-list-item-avatar tile>
                  <v-img :src="getImageUrl(item.product)" contain></v-img>
                </v-list-item-avatar>
                <v-list-item-content>
                  <v-list-item-title>{{ item.product.name }}</v-list-item-title>
                  <v-list-item-subtitle>
                    ¥{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }} = 
                    <strong class="red--text">¥{{ item.subtotal.toFixed(2) }}</strong>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              
              <v-divider class="my-3"></v-divider>
              
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title class="text-h6">订单总额</v-list-item-title>
                  <v-list-item-subtitle class="red--text text-h5 font-weight-bold">
                    ¥{{ detailDialog.order.totalAmount.toFixed(2) }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="detailDialog.show = false">关闭</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      
      <!-- 取消订单对话框 -->
      <v-dialog v-model="cancelDialog.show" max-width="400px">
        <v-card>
          <v-card-title class="error white--text">取消订单</v-card-title>
          <v-card-text class="pt-4">
            <p>您确定要取消订单 <strong>{{ cancelDialog.order?.orderNumber }}</strong> 吗？</p>
            <v-text-field
              v-model="cancelDialog.reason"
              label="取消原因"
              :rules="[v => !!v || '请输入取消原因']"
              required
            ></v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="cancelDialog.show = false">取消</v-btn>
            <v-btn 
              color="error" 
              @click="cancelOrder"
              :loading="cancelDialog.loading"
              :disabled="!cancelDialog.reason"
            >
              确认取消
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
    
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.type === 'error' ? 'error' : 'success'"
      timeout="3000"
    >
      {{ snackbar.message }}
      <v-btn color="white" text @click="snackbar.show = false">关闭</v-btn>
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
      activeTab: 0,
      detailDialog: {
        show: false,
        order: null
      },
      cancelDialog: {
        show: false,
        order: null,
        reason: '',
        loading: false
      },
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  computed: {
    filteredOrders() {
      const statusMap = ['', 'PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPING', 'COMPLETED', 'CANCELLED']
      const filterStatus = statusMap[this.activeTab]
      if (!filterStatus) return this.orders
      return this.orders.filter(order => order.status === filterStatus)
    }
  },
  beforeMount() {
    if (!this.$validateCustomerAuth || !this.$validateCustomerAuth()) {
      this.$router.push('/customer/login')
      return
    }
  },
  mounted() {
    this.fetchOrders()
  },
  methods: {
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    getImageUrl(product) {
      if (!product) return 'https://via.placeholder.com/60x60?text=暂无'
      if (!product.imageUrls && !product.image) {
        return 'https://via.placeholder.com/60x60?text=暂无'
      }
      const imageUrl = product.image || (product.imageUrls ? product.imageUrls.split(',')[0] : '')
      if (imageUrl.startsWith('http')) {
        return imageUrl
      } else if (imageUrl.startsWith('/uploads/')) {
        return 'http://localhost:8080' + imageUrl
      } else {
        return 'http://localhost:8080/images/' + imageUrl
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    getStatusColor(status) {
      const colors = {
        'PENDING': 'orange',
        'CONFIRMED': 'blue',
        'PREPARING': 'cyan',
        'SHIPPING': 'purple',
        'COMPLETED': 'green',
        'CANCELLED': 'red'
      }
      return colors[status] || 'grey'
    },
    
    getStatusText(status) {
      const texts = {
        'PENDING': '待确认',
        'CONFIRMED': '已确认',
        'PREPARING': '备货中',
        'SHIPPING': '已发货',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return texts[status] || status
    },
    
    getTotalQuantity(order) {
      if (!order.orderItems) return 0
      return order.orderItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    
    canCancel(order) {
      // 客户只能在开始发货前取消订单
      return ['PENDING', 'CONFIRMED', 'PREPARING'].includes(order.status)
    },
    
    fetchOrders() {
      this.loading = true
      this.$http.get('/api/orders')
        .then(response => {
          this.orders = response.data
          this.loading = false
        })
        .catch(error => {
          console.error('获取订单失败:', error)
          this.showSnackbar('获取订单失败', 'error')
          this.loading = false
        })
    },
    
    viewProduct(productId) {
      this.$router.push({ name: 'product-detail', params: { id: productId.toString() } })
    },
    
    showOrderDetail(order) {
      this.detailDialog.order = order
      this.detailDialog.show = true
    },
    
    showCancelDialog(order) {
      this.cancelDialog.order = order
      this.cancelDialog.reason = ''
      this.cancelDialog.show = true
    },
    
    cancelOrder() {
      if (!this.cancelDialog.reason) {
        this.showSnackbar('请输入取消原因', 'error')
        return
      }
      
      this.cancelDialog.loading = true
      
      this.$http.put(`/api/orders/${this.cancelDialog.order.id}/cancel?reason=${encodeURIComponent(this.cancelDialog.reason)}`)
        .then(() => {
          this.showSnackbar('订单已取消')
          this.cancelDialog.show = false
          this.cancelDialog.loading = false
          this.fetchOrders()
        })
        .catch(error => {
          console.error('取消订单失败:', error)
          this.showSnackbar(error.response?.data || '取消订单失败', 'error')
          this.cancelDialog.loading = false
        })
    }
  }
}
</script>

<style scoped>
.customer-order-history {
  padding: 20px 0;
}

.order-card {
  transition: box-shadow 0.2s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-item:not(:last-child) {
  border-bottom: 1px dashed #eee;
}
</style>