<template>
  <div class="order-management">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>订单管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/seller/products" text>商品管理</v-btn>
          <v-btn to="/seller/dashboard" text>返回仪表盘</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <!-- 订单筛选标签 -->
      <v-tabs v-model="activeTab" class="mt-4">
        <v-tab>全部订单</v-tab>
        <v-tab>待确认</v-tab>
        <v-tab>待备货</v-tab>
        <v-tab>待发货</v-tab>
        <v-tab>已发货</v-tab>
        <v-tab>已完成</v-tab>
        <v-tab>已取消</v-tab>
      </v-tabs>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>订单列表</h3>
            </v-card-title>
            <v-card-text>
              <v-container v-if="filteredOrders.length > 0">
                <v-data-table
                  :headers="headers"
                  :items="filteredOrders"
                  class="elevation-1"
                  item-key="id"
                >
                  <template v-slot:item.status="{ item }">
                    <v-chip :color="getStatusColor(item.status)" text-color="white">
                      {{ getStatusText(item.status) }}
                    </v-chip>
                  </template>
                  <template v-slot:item.totalAmount="{ item }">
                    ¥{{ item.totalAmount.toFixed(2) }}
                  </template>
                  <template v-slot:item.actions="{ item }">
                    <v-btn
                      v-if="item.status === 'PENDING'"
                      color="success"
                      small
                      @click="confirmOrder(item.id)"
                      class="mr-2"
                    >
                      确认订单
                    </v-btn>
                    <v-btn
                      v-if="item.status === 'CONFIRMED'"
                      color="info"
                      small
                      @click="prepareOrder(item.id)"
                      class="mr-2"
                    >
                      备货完成
                    </v-btn>
                    <v-btn
                      v-if="item.status === 'PREPARING'"
                      color="primary"
                      small
                      @click="shipOrder(item.id)"
                      class="mr-2"
                    >
                      开始发货
                    </v-btn>
                    <v-btn
                      v-if="item.status === 'SHIPPING'"
                      color="success"
                      small
                      @click="completeOrder(item.id)"
                      class="mr-2"
                    >
                      完成交易
                    </v-btn>
                    <v-btn
                      v-if="item.status !== 'COMPLETED' && item.status !== 'CANCELLED'"
                      color="error"
                      small
                      @click="showCancelDialog(item)"
                      class="mr-2"
                    >
                      取消订单
                    </v-btn>
                    <v-btn
                      color="primary"
                      small
                      @click="showOrderDetail(item)"
                    >
                      查看详情
                    </v-btn>
                  </template>
                </v-data-table>
              </v-container>
              <v-container v-else>
                <v-alert type="info" dismissible>
                  暂无订单
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
      
      <!-- 订单详情对话框 -->
      <v-dialog v-model="showOrderDialog" max-width="700px">
        <v-card v-if="selectedOrder">
          <v-card-title>订单详情</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单编号</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedOrder.orderNumber }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单状态</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="getStatusColor(selectedOrder.status)" small text-color="white">
                      {{ getStatusText(selectedOrder.status) }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>收货人</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedOrder.receiverName }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>联系电话</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedOrder.receiverPhone }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>收货地址</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedOrder.shippingAddress }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单备注</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedOrder.remark || '无' }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>下单时间</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(selectedOrder.createTime) }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>订单总额</v-list-item-title>
                  <v-list-item-subtitle class="red--text text--darken-2">¥{{ selectedOrder.totalAmount.toFixed(2) }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-divider class="my-3"></v-divider>
              <v-subheader>商品明细</v-subheader>
              <v-list-item v-for="item in selectedOrder.orderItems" :key="item.id">
                <v-list-item-content>
                  <v-list-item-title>{{ item.product.name }}</v-list-item-title>
                  <v-list-item-subtitle>
                    数量: {{ item.quantity }} × ¥{{ item.unitPrice.toFixed(2) }} = ¥{{ item.subtotal.toFixed(2) }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <div v-if="selectedOrder.status === 'CANCELLED'" class="mt-3">
                <v-divider></v-divider>
                <v-list-item>
                  <v-list-item-content>
                    <v-list-item-title class="error--text">取消信息</v-list-item-title>
                    <v-list-item-subtitle>取消人: {{ selectedOrder.cancelRole === 'CUSTOMER' ? '客户' : '商家' }}</v-list-item-subtitle>
                    <v-list-item-subtitle>取消原因: {{ selectedOrder.cancelReason }}</v-list-item-subtitle>
                    <v-list-item-subtitle>取消时间: {{ formatDate(selectedOrder.cancelTime) }}</v-list-item-subtitle>
                  </v-list-item-content>
                </v-list-item>
              </div>
            </v-list>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="showOrderDialog = false">关闭</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      
      <!-- 取消订单对话框 -->
      <v-dialog v-model="showCancelOrderDialog" max-width="400px">
        <v-card>
          <v-card-title>取消订单</v-card-title>
          <v-card-text>
            <v-text-field
              v-model="cancelReason"
              label="取消原因"
              required
              :rules="[v => !!v || '请输入取消原因']"
            ></v-text-field>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="showCancelOrderDialog = false">取消</v-btn>
            <v-btn color="error" @click="cancelOrder">确认取消</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      
      <!-- 消息提示 -->
      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.type === 'error' ? 'error' : 'success'"
        timeout="3000"
        top
      >
        {{ snackbar.message }}
        <v-btn color="white" text @click="snackbar.show = false">关闭</v-btn>
      </v-snackbar>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  data() {
    return {
      orders: [],
      activeTab: 0,
      showOrderDialog: false,
      showCancelOrderDialog: false,
      selectedOrder: null,
      cancelReason: '',
      headers: [
        { text: '订单编号', value: 'orderNumber' },
        { text: '客户', value: 'customer.username' },
        { text: '订单金额', value: 'totalAmount' },
        { text: '下单时间', value: 'createTime' },
        { text: '状态', value: 'status', sortable: false },
        { text: '操作', value: 'actions', sortable: false }
      ],
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
  mounted() {
    this.fetchOrders()
  },
  methods: {
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    fetchOrders() {
      this.$http.get('/api/seller/orders')
        .then(response => {
          this.orders = response.data
        })
        .catch(error => {
          console.error('获取订单列表失败:', error)
          this.showSnackbar('获取订单列表失败', 'error')
        })
    },
    
    confirmOrder(id) {
      if (confirm('确认接受此订单吗？')) {
        this.$http.put(`/api/seller/orders/${id}/confirm`)
          .then(() => {
            this.fetchOrders()
            this.showSnackbar('订单已确认')
          })
          .catch(error => {
            console.error('确认订单失败:', error)
            this.showSnackbar('确认订单失败', 'error')
          })
      }
    },
    
    prepareOrder(id) {
      if (confirm('确认已备货完成吗？')) {
        this.$http.put(`/api/seller/orders/${id}/prepare`)
          .then(() => {
            this.fetchOrders()
            this.showSnackbar('备货完成')
          })
          .catch(error => {
            console.error('更新订单失败:', error)
            this.showSnackbar('更新订单失败', 'error')
          })
      }
    },
    
    shipOrder(id) {
      if (confirm('确认已开始发货吗？')) {
        this.$http.put(`/api/seller/orders/${id}/ship`)
          .then(() => {
            this.fetchOrders()
            this.showSnackbar('订单已发货')
          })
          .catch(error => {
            console.error('更新订单失败:', error)
            this.showSnackbar('更新订单失败', 'error')
          })
      }
    },
    
    completeOrder(id) {
      if (confirm('确认完成此订单吗？')) {
        this.$http.put(`/api/seller/orders/${id}/complete`)
          .then(() => {
            this.fetchOrders()
            this.showSnackbar('订单已完成')
          })
          .catch(error => {
            console.error('完成订单失败:', error)
            this.showSnackbar('完成订单失败', 'error')
          })
      }
    },
    
    showCancelDialog(order) {
      this.selectedOrder = order
      this.cancelReason = ''
      this.showCancelOrderDialog = true
    },
    
    cancelOrder() {
      if (!this.cancelReason.trim()) {
        this.showSnackbar('请输入取消原因', 'error')
        return
      }
      
      this.$http.put(`/api/seller/orders/${this.selectedOrder.id}/cancel`, {
        reason: this.cancelReason
      })
        .then(() => {
          this.fetchOrders()
          this.showCancelOrderDialog = false
          this.showSnackbar('订单已取消')
        })
        .catch(error => {
          console.error('取消订单失败:', error)
          this.showSnackbar(error.response?.data || '取消订单失败', 'error')
        })
    },
    
    showOrderDetail(order) {
      this.selectedOrder = order
      this.showOrderDialog = true
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
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.order-management {
  padding: 20px 0;
}
</style>