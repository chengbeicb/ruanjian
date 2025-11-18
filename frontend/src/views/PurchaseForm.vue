<template>
  <div class="purchase-form">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md6>
          <v-card>
            <v-card-title class="text-center">
              <h2>购买意向</h2>
            </v-card-title>
            <v-card-text>
              <v-form @submit.prevent="submitPurchase" ref="purchaseForm">
                <v-text-field
                  v-model="form.buyerName"
                  label="姓名"
                  required
                  :rules="[v => !!v || '请输入姓名']"
                ></v-text-field>
                <v-text-field
                  v-model="form.buyerPhone"
                  label="联系电话"
                  required
                  :rules="[v => !!v || '请输入联系电话']"
                ></v-text-field>
                <v-text-field
                  v-model="form.buyerEmail"
                  label="电子邮箱"
                  required
                  :rules="[
                    v => !!v || '请输入电子邮箱',
                    v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || '请输入有效的电子邮箱'
                  ]"
                ></v-text-field>
                <v-text-field
                  v-model="form.buyerAddress"
                  label="联系地址"
                  multiline
                  rows="2"
                  required
                  :rules="[v => !!v || '请输入联系地址']"
                ></v-text-field>
                <v-card-actions>
                  <v-btn type="submit" color="success" :loading="loading">
                    提交购买意向
                  </v-btn>
                  <v-btn @click="goBack" color="primary">
                    返回
                  </v-btn>
                </v-card-actions>
              </v-form>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    
    <!-- 添加Snackbar组件 -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.type"
      :timeout="4000"
      bottom
      right
    >
      {{ snackbar.message }}
      <v-btn color="white" text @click="snackbar.show = false">关闭</v-btn>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'PurchaseForm',
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      form: {
        buyerName: '',
        buyerPhone: '',
        buyerEmail: '',
        buyerAddress: '',
        product: {
          id: this.id
        }
      },
      loading: false,
      // 添加snackbar配置
      snackbar: {
        show: false,
        message: '',
        type: 'info'
      }
    }
  },
  methods: {
    submitPurchase() {
      if (this.$refs.purchaseForm.validate()) {
        this.loading = true;
        // 从 localStorage 获取 customerId
        const customerId = localStorage.getItem('customerId');
        if (!customerId) {
          this.$snackbar.open({ message: '请先登录', type: 'error' });
          this.loading = false;
          this.$router.push({ name: 'customer-login' });
          return;
        }

        if (window.confirm('您确定要提交购买意向吗？')) {
          this.$http.post('/api/purchase-intents', {
            productId: this.id,
            customerId: customerId,
            // 包含表单中的其他信息
            buyerName: this.form.buyerName,
            buyerPhone: this.form.buyerPhone,
            buyerEmail: this.form.buyerEmail,
            buyerAddress: this.form.buyerAddress
          })
            .then(() => {
              this.loading = false;
              this.$snackbar.open({
                message: '购买意向已提交，请等待卖家确认',
                type: 'success'
              });
              this.$router.push({ name: 'home' });
            })
            .catch(error => {
              this.loading = false;
              console.error('购买失败:', error);
              this.$snackbar.open({
                message: `购买失败: ${error.response?.data?.message || error.message}`,
                type: 'error'
              });
            });
        } else {
          this.loading = false;
        }
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    // 添加showSnackbar方法
    showSnackbar(message, type = 'info') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    }
  }
}
</script>

<style scoped>
.purchase-form {
  padding: 20px 0;
}
</style>