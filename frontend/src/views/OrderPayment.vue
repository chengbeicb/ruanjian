<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card v-if="order">
          <v-card-title class="text-h5">订单支付</v-card-title>
          
          <v-card-text>
            <v-row>
              <v-col cols="12">
                <h3>订单信息</h3>
                <v-divider class="my-3"></v-divider>
                <p><strong>订单编号：</strong>{{ order.orderNumber }}</p>
                <p><strong>订单金额：</strong><span class="text-h5 red--text">¥{{ order.totalAmount }}</span></p>
              </v-col>

              <v-col cols="12" class="mt-4">
                <h3>选择支付方式</h3>
                <v-divider class="my-3"></v-divider>
                <v-radio-group v-model="selectedPaymentMethod">
                  <v-radio
                    v-for="method in paymentMethods"
                    :key="method.value"
                    :label="method.label"
                    :value="method.value"
                  >
                    <template v-slot:label>
                      <v-icon left :color="method.color">{{ method.icon }}</v-icon>
                      {{ method.label }}
                    </template>
                  </v-radio>
                </v-radio-group>
              </v-col>
            </v-row>
          </v-card-text>

          <v-card-actions class="pa-4">
            <v-btn large text @click="$router.go(-1)">取消</v-btn>
            <v-spacer></v-spacer>
            <v-btn 
              large 
              color="primary" 
              @click="confirmPayment"
              :loading="processing"
              :disabled="!selectedPaymentMethod"
            >
              确认支付 ¥{{ order.totalAmount }}
            </v-btn>
          </v-card-actions>
        </v-card>

        <!-- 支付成功对话框 -->
        <v-dialog v-model="successDialog" max-width="400" persistent>
          <v-card>
            <v-card-text class="text-center py-8">
              <v-icon size="64" color="success">mdi-check-circle</v-icon>
              <h2 class="mt-4">支付成功！</h2>
              <p class="mt-2">订单已提交，商家将尽快处理</p>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn text @click="goToOrders">查看订单</v-btn>
              <v-btn color="primary" @click="goToHome">返回首页</v-btn>
              <v-spacer></v-spacer>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-col>
    </v-row>

    <snackbar-notification ref="snackbar"></snackbar-notification>
  </v-container>
</template>

<script>
import axios from 'axios';
import SnackbarNotification from '@/components/SnackbarNotification.vue';

export default {
  name: 'OrderPayment',
  components: {
    SnackbarNotification
  },
  data() {
    return {
      order: null,
      selectedPaymentMethod: 'ALIPAY',
      processing: false,
      successDialog: false,
      paymentMethods: [
        { label: '支付宝', value: 'ALIPAY', icon: 'mdi-alpha-a-circle', color: 'blue' },
        { label: '微信支付', value: 'WECHAT', icon: 'mdi-wechat', color: 'green' },
        { label: '银行卡', value: 'BANK_CARD', icon: 'mdi-credit-card', color: 'orange' },
        { label: '信用卡', value: 'CREDIT_CARD', icon: 'mdi-credit-card-outline', color: 'purple' }
      ]
    };
  },
  mounted() {
    this.loadOrder();
  },
  methods: {
    async loadOrder() {
      const orderId = this.$route.params.orderId;
      try {
        const response = await axios.get(`/api/orders/${orderId}`);
        this.order = response.data;
        
        // 检查订单状态
        if (this.order.paymentStatus === 'PAID') {
          this.$refs.snackbar.show('订单已支付', 'warning');
          setTimeout(() => {
            this.$router.push('/orders');
          }, 1500);
        }
      } catch (error) {
        this.$refs.snackbar.show('加载订单失败', 'error');
      }
    },
    async confirmPayment() {
      this.processing = true;
      try {
        // 创建支付记录
        const createResponse = await axios.post('/api/payments/create', {
          orderId: this.order.id,
          paymentMethod: this.selectedPaymentMethod
        });
        
        const payment = createResponse.data;
        
        // 模拟支付处理（实际项目中会跳转到第三方支付页面）
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // 执行支付
        await axios.post(`/api/payments/${payment.id}/pay`);
        
        this.successDialog = true;
      } catch (error) {
        this.$refs.snackbar.show(error.response?.data?.message || '支付失败', 'error');
      } finally {
        this.processing = false;
      }
    },
    goToOrders() {
      this.$router.push('/orders');
    },
    goToHome() {
      this.$router.push('/');
    }
  }
};
</script>
