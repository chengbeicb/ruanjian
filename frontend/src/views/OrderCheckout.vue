<template>
  <div class="order-checkout">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>确认订单</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/customer/cart" text>返回购物车</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <v-layout mt-6>
        <v-flex xs12 sm10 md8 offset-sm1 offset-md2>
          <!-- 商品清单 -->
          <v-card class="mb-4">
            <v-card-title>
              <v-icon left>mdi-package-variant</v-icon>
              商品清单
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <v-container v-if="loading">
                <v-row justify="center">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </v-row>
              </v-container>
              
              <v-simple-table v-else-if="checkoutItems.length > 0">
                <template v-slot:default>
                  <thead>
                    <tr>
                      <th class="text-left">商品</th>
                      <th class="text-center">单价</th>
                      <th class="text-center">数量</th>
                      <th class="text-right">小计</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in checkoutItems" :key="item.id">
                      <td>
                        <div class="d-flex align-center py-2">
                          <v-img
                            :src="getImageUrl(item.product)"
                            max-width="60"
                            max-height="60"
                            class="mr-3"
                            contain
                          ></v-img>
                          <span>{{ item.product.name }}</span>
                        </div>
                      </td>
                      <td class="text-center">¥{{ item.product.price.toFixed(2) }}</td>
                      <td class="text-center">{{ item.quantity }}</td>
                      <td class="text-right red--text font-weight-bold">
                        ¥{{ (item.product.price * item.quantity).toFixed(2) }}
                      </td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colspan="3" class="text-right font-weight-bold">商品总计:</td>
                      <td class="text-right red--text text-h6 font-weight-bold">
                        ¥{{ totalAmount.toFixed(2) }}
                      </td>
                    </tr>
                  </tfoot>
                </template>
              </v-simple-table>
              
              <v-alert v-else type="warning">
                没有要结算的商品，请返回购物车选择商品
              </v-alert>
            </v-card-text>
          </v-card>
          
          <!-- 收货信息 -->
          <v-card class="mb-4" v-if="checkoutItems.length > 0">
            <v-card-title>
              <v-icon left>mdi-map-marker</v-icon>
              收货信息
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <v-form ref="orderForm" v-model="formValid">
                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="orderForm.receiverName"
                      label="收货人姓名"
                      :rules="[v => !!v || '请输入收货人姓名']"
                      required
                      prepend-icon="mdi-account"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="orderForm.receiverPhone"
                      label="联系电话"
                      :rules="[
                        v => !!v || '请输入联系电话',
                        v => /^1[3-9]\d{9}$/.test(v) || '请输入正确的手机号'
                      ]"
                      required
                      prepend-icon="mdi-phone"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12">
                    <v-textarea
                      v-model="orderForm.shippingAddress"
                      label="收货地址"
                      :rules="[v => !!v || '请输入收货地址']"
                      required
                      prepend-icon="mdi-home"
                      rows="2"
                    ></v-textarea>
                  </v-col>
                  <v-col cols="12">
                    <v-textarea
                      v-model="orderForm.remark"
                      label="订单备注（选填）"
                      prepend-icon="mdi-note"
                      rows="2"
                      placeholder="如有特殊要求请在此备注"
                    ></v-textarea>
                  </v-col>
                </v-row>
              </v-form>
            </v-card-text>
          </v-card>
          
          <!-- 提交订单 -->
          <v-card v-if="checkoutItems.length > 0">
            <v-card-text>
              <v-row align="center">
                <v-col cols="6">
                  <span class="text-body-1">
                    共 <strong class="primary--text">{{ totalQuantity }}</strong> 件商品
                  </span>
                </v-col>
                <v-col cols="6" class="text-right">
                  <span class="text-h6 mr-4">
                    应付金额: <strong class="red--text">¥{{ totalAmount.toFixed(2) }}</strong>
                  </span>
                  <v-btn 
                    color="success" 
                    large
                    :loading="submitting"
                    :disabled="!formValid || submitting"
                    @click="submitOrder"
                  >
                    <v-icon left>mdi-check</v-icon>
                    提交订单
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    
    <!-- 订单成功对话框 -->
    <v-dialog v-model="successDialog.show" max-width="400px" persistent>
      <v-card>
        <v-card-title class="success white--text">
          <v-icon left color="white">mdi-check-circle</v-icon>
          订单提交成功
        </v-card-title>
        <v-card-text class="pt-4">
          <div class="text-center">
            <v-icon size="64" color="success">mdi-check-circle-outline</v-icon>
            <div class="mt-4 text-h6">恭喜您，订单提交成功！</div>
            <div class="mt-2 text-body-2 grey--text">
              订单编号: {{ successDialog.orderNumber }}
            </div>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="goToOrders">查看订单</v-btn>
          <v-btn text @click="goToHome">继续购物</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
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
  name: 'OrderCheckout',
  data() {
    return {
      checkoutItems: [],
      cartIds: [],
      loading: false,
      submitting: false,
      formValid: false,
      orderForm: {
        receiverName: '',
        receiverPhone: '',
        shippingAddress: '',
        remark: ''
      },
      successDialog: {
        show: false,
        orderNumber: ''
      },
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  computed: {
    totalQuantity() {
      return this.checkoutItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalAmount() {
      return this.checkoutItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
    }
  },
  beforeMount() {
    if (!this.$validateCustomerAuth || !this.$validateCustomerAuth()) {
      this.$router.push('/customer/login')
      return
    }
    
    // 获取从购物车传来的cartIds
    const storedCartIds = sessionStorage.getItem('checkoutCartIds')
    if (!storedCartIds) {
      this.$router.push('/customer/cart')
      return
    }
    
    this.cartIds = JSON.parse(storedCartIds)
    if (this.cartIds.length === 0) {
      this.$router.push('/customer/cart')
      return
    }
  },
  mounted() {
    this.fetchCheckoutItems()
  },
  methods: {
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    getImageUrl(product) {
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
    
    fetchCheckoutItems() {
      this.loading = true
      // 获取购物车信息，然后筛选出选中的项
      this.$http.get('/api/cart')
        .then(response => {
          this.checkoutItems = response.data.filter(item => this.cartIds.includes(item.id))
          this.loading = false
          
          if (this.checkoutItems.length === 0) {
            this.showSnackbar('所选商品已不存在', 'error')
            this.$router.push('/customer/cart')
          }
        })
        .catch(error => {
          console.error('获取结算商品失败:', error)
          this.showSnackbar('获取商品信息失败', 'error')
          this.loading = false
        })
    },
    
    submitOrder() {
      if (!this.$refs.orderForm.validate()) {
        this.showSnackbar('请填写完整的收货信息', 'error')
        return
      }
      
      this.submitting = true
      
      const orderData = {
        cartIds: this.cartIds,
        receiverName: this.orderForm.receiverName,
        receiverPhone: this.orderForm.receiverPhone,
        shippingAddress: this.orderForm.shippingAddress,
        remark: this.orderForm.remark
      }
      
      this.$http.post('/api/orders', orderData)
        .then(response => {
          this.submitting = false
          // 清除sessionStorage
          sessionStorage.removeItem('checkoutCartIds')
          // 显示成功对话框
          this.successDialog.orderNumber = response.data.orderNumber
          this.successDialog.show = true
        })
        .catch(error => {
          this.submitting = false
          console.error('提交订单失败:', error)
          this.showSnackbar(error.response?.data || '提交订单失败，请重试', 'error')
        })
    },
    
    goToOrders() {
      this.successDialog.show = false
      this.$router.push('/customer/orders')
    },
    
    goToHome() {
      this.successDialog.show = false
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.order-checkout {
  padding: 20px 0;
}
</style>
