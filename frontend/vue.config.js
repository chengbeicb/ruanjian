module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 代理目标，指向您的后端服务
        ws: true, // 是否代理 websockets
        changeOrigin: true // 设置为 true，服务器收到的请求头中的 host 字段将为目标 URL
      },
      '/images': {
        target: 'http://localhost:8080', // 代理图片请求到后端
        ws: false,
        changeOrigin: true
      }
    }
  }
};
