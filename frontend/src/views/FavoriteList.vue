<template>
  <div class="favorite-list">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>我的收藏</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/" text>返回首页</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>收藏商品列表</h3>
            </v-card-title>
            <v-card-text>
              <v-container v-if="loading">
                <v-row justify="center">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </v-row>
              </v-container>
              
              <v-container v-else-if="favorites.length > 0">
                <v-row>
                  <v-col 
                    v-for="item in favorites" 
                    :key="item.id" 
                    cols="12" 
                    sm="6" 
                    md="4" 
                    lg="3"
                  >
                    <v-card hover class="favorite-item">
                      <v-img
                        :src="getImageUrl(item.product)"
                        aspect-ratio="1"
                        class="grey lighten-2"
                        height="200"
                        contain
                      >
                        <template v-slot:placeholder>
                          <v-row class="fill-height ma-0" align="center" justify="center">
                            <v-progress-circular indeterminate color="grey lighten-1"></v-progress-circular>
                          </v-row>
                        </template>
                      </v-img>
                      
                      <v-card-title class="text-subtitle-1 font-weight-bold">
                        {{ item.product.name }}
                      </v-card-title>
                      
                      <v-card-text>
                        <div class="d-flex align-center justify-space-between">
                          <span class="red--text text-h6 font-weight-bold">
                            ¥{{ item.product.price.toFixed(2) }}
                          </span>
                          <v-chip 
                            small 
                            :color="item.product.stock > 0 ? 'success' : 'error'" 
                            text-color="white"
                          >
                            {{ item.product.stock > 0 ? '有货' : '缺货' }}
                          </v-chip>
                        </div>
                        <div class="text-caption grey--text mt-2">
                          收藏时间: {{ formatDate(item.createTime) }}
                        </div>
                      </v-card-text>
                      
                      <v-divider></v-divider>
                      
                      <v-card-actions>
                        <v-btn 
                          text 
                          color="primary" 
                          small
                          @click="viewProduct(item.product.id)"
                        >
                          <v-icon left small>mdi-eye</v-icon>
                          查看
                        </v-btn>
                        <v-btn 
                          text 
                          color="success" 
                          small
                          :disabled="item.product.stock <= 0"
                          @click="addToCart(item.product)"
                        >
                          <v-icon left small>mdi-cart-plus</v-icon>
                          加购
                        </v-btn>
                        <v-spacer></v-spacer>
                        <v-btn 
                          icon 
                          color="error" 
                          small
                          @click="removeFavorite(item.product.id)"
                        >
                          <v-icon>mdi-heart-broken</v-icon>
                        </v-btn>
                      </v-card-actions>
                    </v-card>
                  </v-col>
                </v-row>
              </v-container>
              
              <v-container v-else>
                <v-alert type="info">
                  您还没有收藏任何商品
                  <template v-slot:append>
                    <v-btn to="/" color="primary" text>去逛逛</v-btn>
                  </template>
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    
    <!-- 加入购物车数量对话框 -->
    <v-dialog v-model="cartDialog.show" max-width="400px">
      <v-card>
        <v-card-title>加入购物车</v-card-title>
        <v-card-text>
          <div class="mb-4">
            <strong>{{ cartDialog.product?.name }}</strong>
          </div>
          <div class="mb-4">
            <span>单价: </span>
            <span class="red--text font-weight-bold">¥{{ cartDialog.product?.price?.toFixed(2) }}</span>
          </div>
          <v-text-field
            v-model.number="cartDialog.quantity"
            label="数量"
            type="number"
            min="1"
            :max="cartDialog.product?.stock || 99"
            :rules="[v => v > 0 || '数量必须大于0', v => v <= (cartDialog.product?.stock || 99) || '超过库存限制']"
          ></v-text-field>
          <div class="text-caption grey--text">
            库存: {{ cartDialog.product?.stock || 0 }} 件
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="cartDialog.show = false">取消</v-btn>
          <v-btn color="success" @click="confirmAddToCart">确认加入</v-btn>
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
  name: 'FavoriteList',
  data() {
    return {
      favorites: [],
      loading: false,
      cartDialog: {
        show: false,
        product: null,
        quantity: 1
      },
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  beforeMount() {
    if (!this.$validateCustomerAuth || !this.$validateCustomerAuth()) {
      this.$router.push('/customer/login')
      return
    }
  },
  mounted() {
    this.fetchFavorites()
  },
  methods: {
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    getImageUrl(product) {
      if (!product.imageUrls && !product.image) {
        return 'https://via.placeholder.com/200x200?text=暂无图片'
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
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    fetchFavorites() {
      this.loading = true
      this.$http.get('/api/favorites')
        .then(response => {
          this.favorites = response.data
          this.loading = false
        })
        .catch(error => {
          console.error('获取收藏列表失败:', error)
          this.showSnackbar('获取收藏列表失败', 'error')
          this.loading = false
        })
    },
    
    viewProduct(productId) {
      this.$router.push({ name: 'product-detail', params: { id: productId.toString() } })
    },
    
    addToCart(product) {
      this.cartDialog.product = product
      this.cartDialog.quantity = 1
      this.cartDialog.show = true
    },
    
    confirmAddToCart() {
      if (this.cartDialog.quantity < 1) {
        this.showSnackbar('数量必须大于0', 'error')
        return
      }
      if (this.cartDialog.quantity > this.cartDialog.product.stock) {
        this.showSnackbar('数量不能超过库存', 'error')
        return
      }
      
      this.$http.post('/api/cart', {
        productId: this.cartDialog.product.id,
        quantity: this.cartDialog.quantity
      })
        .then(() => {
          this.showSnackbar('已加入购物车')
          this.cartDialog.show = false
        })
        .catch(error => {
          console.error('加入购物车失败:', error)
          this.showSnackbar(error.response?.data || '加入购物车失败', 'error')
        })
    },
    
    removeFavorite(productId) {
      if (confirm('确定要取消收藏该商品吗？')) {
        this.$http.delete(`/api/favorites/${productId}`)
          .then(() => {
            this.showSnackbar('已取消收藏')
            this.fetchFavorites()
          })
          .catch(error => {
            console.error('取消收藏失败:', error)
            this.showSnackbar('取消收藏失败', 'error')
          })
      }
    }
  }
}
</script>

<style scoped>
.favorite-list {
  padding: 20px 0;
}

.favorite-item {
  transition: transform 0.2s, box-shadow 0.2s;
}

.favorite-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
