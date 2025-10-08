<template>
  <div class="seller-login">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md4>
          <v-card>
            <v-card-title class="text-center">
              <h2>卖家登录</h2>
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
                  <v-btn type="submit" color="primary" block :loading="loading">
                    登录
                  </v-btn>
                </v-card-actions>
                <v-alert v-if="error" type="error" dismissible @input="error = ''">
                  {{ error }}
                </v-alert>
              </v-form>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'SellerLogin',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      error: '',
      loading: false
    }
  },
  methods: {
    login() {
      if (this.$refs.loginForm.validate()) {
        this.loading = true
        this.error = ''
        
        // 直接使用Basic认证访问受保护资源
        const auth = btoa(`${this.form.username}:${this.form.password}`)
        this.$http.get('/seller/me', {  // 移除重复的/api前缀
          headers: {
            Authorization: `Basic ${auth}`
          }
        })
          .then(response => {
            // 验证成功，存储凭证并跳转
            localStorage.setItem('auth', auth)
            this.loading = false
            this.$router.push('/seller/dashboard')
          })
          .catch(error => {
            console.error('登录失败:', error)
            this.loading = false
            this.error = error.response?.data || '登录失败，请检查用户名和密码'
          })
      }
    }
  }
}
</script>

<style scoped>
.seller-login {
  padding: 40px 0;
  min-height: calc(100vh - 150px);
  display: flex;
  align-items: center;
}
</style>