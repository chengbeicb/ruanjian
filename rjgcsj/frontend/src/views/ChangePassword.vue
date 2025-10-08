<template>
  <div class="change-password">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md4>
          <v-card>
            <v-card-title class="text-center">
              <h2>修改密码</h2>
            </v-card-title>
            <v-card-text>
              <v-form @submit.prevent="changePassword" ref="passwordForm">
                <v-text-field
                  v-model="form.oldPassword"
                  label="当前密码"
                  type="password"
                  required
                  :rules="[v => !!v || '请输入当前密码']"
                ></v-text-field>
                <v-text-field
                  v-model="form.newPassword"
                  label="新密码"
                  type="password"
                  required
                  :rules="[
                    v => !!v || '请输入新密码',
                    v => v.length >= 6 || '新密码长度至少为6位'
                  ]"
                ></v-text-field>
                <v-text-field
                  v-model="form.confirmPassword"
                  label="确认新密码"
                  type="password"
                  required
                  :rules="[
                    v => !!v || '请确认新密码',
                    v => v === form.newPassword || '两次输入的密码不一致'
                  ]"
                ></v-text-field>
                <v-card-actions>
                  <v-btn type="submit" color="primary" block :loading="loading">
                    确认修改
                  </v-btn>
                  <v-btn to="/seller/dashboard" color="secondary" block>
                    返回仪表盘
                  </v-btn>
                </v-card-actions>
                <v-alert v-if="error" type="error" dismissible @input="error = ''">
                  {{ error }}
                </v-alert>
                <v-alert v-if="success" type="success" dismissible @input="success = ''">
                  {{ success }}
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
  name: 'ChangePassword',
  data() {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      error: '',
      success: '',
      loading: false
    }
  },
  methods: {
    changePassword() {
      if (this.$refs.passwordForm.validate()) {
        this.loading = true
        this.error = ''
        this.success = ''
        
        this.$http.put('/seller/password', {
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })
          .then(response => {
            this.loading = false
            this.success = '密码修改成功'
            // 重置表单
            this.form = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
            this.$refs.passwordForm.reset()
          })
          .catch(error => {
            this.loading = false
            console.error('修改密码失败:', error)
            this.error = error.response?.data || '修改密码失败'
          })
      }
    }
  }
}
</script>

<style scoped>
.change-password {
  padding: 40px 0;
  min-height: calc(100vh - 150px);
  display: flex;
  align-items: center;
}
</style>