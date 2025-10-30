<template>
  <div class="customer-management">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>客户管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/seller/dashboard" text>返回仪表盘</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>客户列表</h3>
            </v-card-title>
            <v-card-text>
              <!-- 搜索功能 -->
              <v-form @submit.prevent="searchCustomers">
                <v-layout row wrap>
                  <v-flex xs12 sm6>
                    <v-text-field
                      v-model="searchQuery"
                      label="搜索客户（用户名/手机号）"
                      placeholder="输入关键词搜索"
                    ></v-text-field>
                  </v-flex>
                  <v-flex xs12 sm6 class="text-right">
                    <v-btn type="submit" color="primary" class="mr-2">搜索</v-btn>
                    <v-btn @click="resetSearch" color="secondary">重置</v-btn>
                  </v-flex>
                </v-layout>
              </v-form>
              
              <!-- 客户列表 -->
              <v-container v-if="customers.length > 0">
                <v-data-table
                  :headers="headers"
                  :items="customers"
                  :loading="loading"
                  class="elevation-1"
                  hide-actions
                >
                  <template v-slot:items="{ item }">
                    <td>{{ item.username }}</td>
                    <td>{{ item.phone || '-' }}</td>
                    <td>{{ item.defaultAddress || '-' }}</td>
                    <td>{{ formatDateTime(item.createTime) }}</td>
                    <td>
                      <v-chip :color="item.active ? 'success' : 'error'" text-color="white">
                        {{ item.active ? '活跃' : '禁用' }}
                      </v-chip>
                    </td>
                    <td>
                      <v-btn @click="viewCustomerOrders(item.id, item.username)" color="primary" small>查看订单</v-btn>
                    </td>
                  </template>
                </v-data-table>
              </v-container>
              <v-container v-else-if="!loading">
                <v-alert type="info" dismissible>
                  暂无客户信息
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
      
      <!-- 客户订单对话框 -->
      <v-dialog v-model="showOrdersDialog" max-width="800px">
        <v-card>
          <v-card-title>{{ selectedCustomerName }} 的订单记录</v-card-title>
          <v-card-text>
            <v-container v-if="customerOrders.length > 0">
              <v-layout wrap>
                <v-flex xs12 v-for="order in customerOrders" :key="order.id">
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
                      </v-flex>
                    </v-layout>
                  </v-card>
                </v-flex>
              </v-layout>
            </v-container>
            <v-container v-else>
              <v-alert type="info" dismissible>
                该客户暂无订单记录
              </v-alert>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-btn @click="showOrdersDialog = false" color="primary">关闭</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
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
  name: 'CustomerManagement',
  data() {
    return {
      customers: [],
      searchQuery: '',
      loading: false,
      showOrdersDialog: false,
      selectedCustomerId: null,
      selectedCustomerName: '',
      customerOrders: [],
      headers: [
        { text: '用户名', value: 'username' },
        { text: '手机号', value: 'phone' },
        { text: '默认交易地点', value: 'defaultAddress' },
        { text: '注册时间', value: 'createTime' },
        { text: '状态', value: 'active' },
        { text: '操作', value: 'actions' }
      ],
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  mounted() {
    this.fetchCustomers()
  },
  methods: {
    fetchCustomers() {
      this.loading = true
      this.$http.get('/seller/customers', { params: { keyword: this.searchQuery || undefined } })
        .then(response => {
          this.loading = false
          this.customers = response.data
        })
        .catch(error => {
          this.loading = false
          this.snackbar = {
            show: true,
            message: error.response?.data || '获取客户列表失败',
            type: 'error'
          }
        })
    },
    searchCustomers() {
      this.fetchCustomers()
    },
    resetSearch() {
      this.searchQuery = ''
      this.fetchCustomers()
    },
    viewCustomerOrders(customerId, customerName) {
      this.selectedCustomerId = customerId
      this.selectedCustomerName = customerName
      this.fetchCustomerOrders()
      this.showOrdersDialog = true
    },
    fetchCustomerOrders() {
      this.$http.get(`/seller/customers/${this.selectedCustomerId}/orders`)
        .then(response => {
          this.customerOrders = response.data
        })
        .catch(error => {
          this.snackbar = {
            show: true,
            message: error.response?.data || '获取客户订单失败',
            type: 'error'
          }
        })
    },
    getFirstImageUrl(imageUrls) {
      if (!imageUrls) return 'https://via.placeholder.com/400x400?text=暂无图片'
      
      const firstUrl = imageUrls.split(',')[0]
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
    }
  }
}
</script>

<style scoped>
.customer-management {
  padding: 20px 0;
}
</style>