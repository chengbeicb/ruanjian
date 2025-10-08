<template>
  <div>
    <v-snackbar
      v-model="snackbar"
      :color="color"
      :timeout="timeout"
      :top="true"
      right
    >
      <v-icon left>{{ icon }}</v-icon>
      {{ message }}
      <v-btn color="white" text @click="snackbar = false">关闭</v-btn>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'SnackbarNotification',
  data() {
    return {
      snackbar: false,
      message: '',
      color: 'success',
      timeout: 6000,
      icon: 'mdi-check'
    }
  },
  methods: {
    open(options) {
      this.message = options.message || ''
      this.color = options.type === 'error' ? 'error' : 'success'
      this.icon = options.type === 'error' ? 'mdi-alert-circle' : 'mdi-check'
      this.timeout = options.timeout || 6000
      this.snackbar = true
    }
  }
}
</script>