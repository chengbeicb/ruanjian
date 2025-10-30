<template>
  <div class="customer-login">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md6>
          <v-card>
            <v-card-title class="text-center">
              <h2>客户登录</h2>
            </v-card-title>
            <v-card-text>
              <v-form @submit.prevent="login" ref="loginForm">
                <v-text-field
                  v-model="form.username"
                  label="用户名"
                  required
                  :rules="[v => !!v || '请输入用户名']"
                ></v-text-field>
                <v-text-field
                  v-model="form.password"
                  label="密码"
                  type="password"
                  required
                  :rules="[v => !!v || '请输入密码']"
                ></v-text-field>
                <v-card-actions>
                  <v-btn type="submit" color="primary" block :loading="loggingIn">
                    登录
                  </v-btn>
                  <v-btn @click="goToRegister" color="secondary" block text>
                    没有账号？去注册
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
  name: 'CustomerLogin',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loggingIn: false,
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  methods: {
    login() {
      if (!this.$refs.loginForm.validate()) {
        return
      }
      
      this.loggingIn = true
      const credentials = {
        username: this.form.username,
        password: this.form.password
      }
      
      this.$http.post('/customer/login', credentials)
        .then(() => {
          this.loggingIn = false
          // 保存登录状态到本地存储
          localStorage.setItem('auth', 'customer')
          localStorage.setItem('customerUsername', this.form.username)
          this.snackbar = {
            show: true,
            message: '登录成功',
            type: 'success'
          }
          // 登录成功后跳转到首页
          this.$router.push('/')
        })
        .catch(error => {
          this.loggingIn = false
          this.snackbar = {
            show: true,
            message: error.response?.data || '登录失败，请检查用户名和密码',
            type: 'error'
          }
        })
    },
    goToRegister() {
      this.$router.push('/customer/register')
    }
  }
}
</script>

<style scoped>
.customer-login {
  padding: 40px 0;
}
</style>