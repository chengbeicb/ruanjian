<template>
  <div class="customer-register">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md6>
          <v-card>
            <v-card-title class="text-center">
              <h2>客户注册</h2>
            </v-card-title>
            <v-card-text>
              <v-form @submit.prevent="register" ref="registerForm">
                <v-text-field
                  v-model="form.username"
                  label="用户名"
                  required
                  :rules="[v => !!v || '请输入用户名', v => v.length >= 4 || '用户名至少4个字符']"
                ></v-text-field>
                <v-text-field
                  v-model="form.password"
                  label="密码"
                  type="password"
                  required
                  :rules="[v => !!v || '请输入密码', v => v.length >= 6 || '密码至少6个字符']"
                ></v-text-field>
                <v-text-field
                  v-model="form.confirmPassword"
                  label="确认密码"
                  type="password"
                  required
                  :rules="[v => !!v || '请确认密码', v => v === form.password || '两次输入的密码不一致']"
                ></v-text-field>
                <v-text-field
                  v-model="form.phone"
                  label="手机号"
                  required
                  :rules="[v => !!v || '请输入手机号', v => /^1[3-9]\d{9}$/.test(v) || '请输入有效的手机号']"
                ></v-text-field>
                <v-text-field
                  v-model="form.defaultAddress"
                  label="默认交易地点"
                  required
                  :rules="[v => !!v || '请输入默认交易地点']"
                ></v-text-field>
                <v-card-actions>
                  <v-btn type="submit" color="primary" block :loading="registering">
                    注册
                  </v-btn>
                  <v-btn @click="goToLogin" color="secondary" block text>
                    已有账号？去登录
                  </v-btn>
                </v-card-actions>
              </v-form>
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
  name: 'CustomerRegister',
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        phone: '',
        defaultAddress: ''
      },
      registering: false,
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  methods: {
    register() {
      if (!this.$refs.registerForm.validate()) {
        return
      }
      
      this.registering = true
      const customerData = {
        username: this.form.username,
        password: this.form.password,
        phone: this.form.phone,
        defaultAddress: this.form.defaultAddress
      }
      
      this.$http.post('/customer/register', customerData)
        .then(() => {
          this.registering = false
          this.snackbar = {
            show: true,
            message: '注册成功，请登录',
            type: 'success'
          }
          setTimeout(() => {
            this.$router.push('/customer/login')
          }, 2000)
        })
        .catch(error => {
          this.registering = false
          this.snackbar = {
            show: true,
            message: error.response?.data || '注册失败，请稍后重试',
            type: 'error'
          }
        })
    },
    goToLogin() {
      this.$router.push('/customer/login')
    }
  }
}
</script>

<style scoped>
.customer-register {
  padding: 40px 0;
}
</style>