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
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>购买意向列表</h3>
            </v-card-title>
            <v-card-text>
              <v-container v-if="purchaseIntents.length > 0">
                <v-data-table
                  :headers="headers"
                  :items="purchaseIntents"
                  class="elevation-1"
                  item-key="id"
                >
                  <template v-slot:item.status="{ item }">
                    <v-chip :color="getStatusColor(item)" text-color="white">
                      {{ getStatusText(item) }}
                    </v-chip>
                  </template>
                  <template v-slot:item.actions="{ item }">
                    <v-btn
                      v-if="!item.completed && !item.canceled"
                      color="success"
                      @click="completePurchase(item.id)"
                    >
                      完成交易
                    </v-btn>
                    <v-btn
                      v-if="!item.completed && !item.canceled"
                      color="error"
                      @click="cancelPurchase(item.id)"
                    >
                      取消交易
                    </v-btn>
                    <v-btn
                      @click="showBuyerInfo(item)"
                      color="primary"
                    >
                      查看买家信息
                    </v-btn>
                  </template>
                </v-data-table>
              </v-container>
              <v-container v-else>
                <v-alert type="info" dismissible>
                  暂无购买意向
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
      
      <!-- 买家信息对话框 -->
      <v-dialog v-model="showBuyerInfoDialog" max-width="500px">
        <v-card v-if="selectedIntent">
          <v-card-title>买家信息</v-card-title>
          <v-card-text>
            <v-list>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>商品名称</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedIntent.product.name }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>买家姓名</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedIntent.buyerName }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>联系电话</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedIntent.buyerPhone }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>电子邮箱</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedIntent.buyerEmail }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>联系地址</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedIntent.buyerAddress }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title>提交时间</v-list-item-title>
                  <v-list-item-subtitle>{{ formatDate(selectedIntent.createTime) }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="showBuyerInfoDialog = false">关闭</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  // 在data()中添加snackbar配置
  data() {
    return {
      purchaseIntents: [],
      showBuyerInfoDialog: false,
      selectedIntent: null,
      headers: [
        { text: 'ID', value: 'id' },
        { text: '商品名称', value: 'product.name' },
        { text: '买家姓名', value: 'buyerName' },
        { text: '提交时间', value: 'createTime', sortable: false },
        { text: '状态', value: 'status', sortable: false },
        { text: '操作', value: 'actions', sortable: false }
      ],
      // 添加snackbar配置
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  mounted() {
    this.fetchPurchaseIntents()
  },
  // 在methods中添加showSnackbar方法
  methods: {
    // 添加showSnackbar方法
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    fetchPurchaseIntents() {
      this.$http.get('/purchase-intents')
        .then(response => {
          this.purchaseIntents = response.data
        })
        .catch(error => {
          console.error('获取购买意向列表失败:', error)
          // 修改为使用showSnackbar方法
          this.showSnackbar('获取购买意向列表失败', 'error')
        })
    },
    completePurchase(id) {
      if (confirm('确认完成此交易吗？完成后商品将下架。')) {
        this.$http.put(`/purchase-intents/${id}/complete`)
          .then(response => {
            this.fetchPurchaseIntents()
            // 修改为使用showSnackbar方法
            this.showSnackbar('交易已完成')
          })
          .catch(error => {
            console.error('完成交易失败:', error)
            // 修改为使用showSnackbar方法
            this.showSnackbar('完成交易失败', 'error')
          })
      }
    },
    cancelPurchase(id) {
      if (confirm('确认取消此交易吗？取消后商品将解冻。')) {
        this.$http.put(`/purchase-intents/${id}/cancel`)
          .then(response => {
            this.fetchPurchaseIntents()
            // 修改为使用showSnackbar方法
            this.showSnackbar('交易已取消')
          })
          .catch(error => {
            console.error('取消交易失败:', error)
            // 修改为使用showSnackbar方法
            this.showSnackbar('取消交易失败', 'error')
          })
      }
    },
    showBuyerInfo(intent) {
      this.selectedIntent = intent
      this.showBuyerInfoDialog = true
    },
    getStatusColor(item) {
      if (item.completed) return 'success'
      if (item.canceled) return 'error'
      return 'warning'
    },
    getStatusText(item) {
      if (item.completed) return '已完成'
      if (item.canceled) return '已取消'
      return '处理中'
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

// 在模板中添加v-snackbar组件
// 在v-container标签后添加：
<v-snackbar
  v-model="snackbar.show"
  :color="snackbar.type === 'error' ? 'error' : 'success'"
  timeout="3000"
  top
>
  {{ snackbar.message }}
  <v-btn color="accent" text @click="snackbar.show = false">关闭</v-btn>
</v-snackbar>