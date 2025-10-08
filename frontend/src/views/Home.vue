<template>
  <div class="home">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md6>
          <v-card>
            <v-card-title class="text-center">
              <h2>商品展示</h2>
            </v-card-title>
            <v-card-text>
              <v-container v-if="products.length > 0">
                <v-layout wrap>
                  <v-flex xs12 v-for="product in products" :key="product.id">
                    <v-card hover class="mb-4">
                      <v-img
                        v-if="product.imageUrl"
                        :src="product.imageUrl"
                        aspect-ratio="16/9"
                        class="grey lighten-2"
                      ></v-img>
                      <v-img
                        v-else
                        src="https://via.placeholder.com/800x450?text=暂无图片"
                        aspect-ratio="16/9"
                        class="grey lighten-2"
                      ></v-img>
                      <v-card-title>{{ product.name }}</v-card-title>
                      <v-card-text>{{ product.description }}</v-card-text>
                      <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-chip color="primary" text-color="white">¥{{ product.price.toFixed(2) }}</v-chip>
                        <v-btn
                          :disabled="!product.available || product.frozen"
                          color="success"
                          @click="viewProduct(product.id)"
                        >
                          {{ product.frozen ? '有人正在购买' : '查看详情' }}
                        </v-btn>
                      </v-card-actions>
                    </v-card>
                  </v-flex>
                </v-layout>
              </v-container>
              <v-container v-else>
                <v-alert type="info" dismissible>
                  暂无可用商品
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    
    <!-- 添加v-snackbar组件在根元素内部 -->
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
  name: 'Home',
  data() {
    return {
      products: [],
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  mounted() {
    this.fetchProducts()
  },
  methods: {
    fetchProducts() {
      this.$http.get('/products')
        .then(response => {
          // 添加数据转换逻辑
          this.products = response.data.map(product => ({
            ...product,
            available: product.status === 'AVAILABLE',
            frozen: product.status === 'FROZEN'
          }))
        })
        .catch(error => {
          console.error('获取商品列表失败:', error)
          this.snackbar = {
            show: true,
            message: '获取商品列表失败',
            type: 'error'
          }
        })
    },
    viewProduct(productId) {
      this.$router.push({ name: 'product-detail', params: { id: productId } })
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px 0;
}
</style>