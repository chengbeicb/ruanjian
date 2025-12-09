<template>
  <div class="shopping-cart">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>我的购物车</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn to="/" text>返回首页</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>购物车商品</h3>
              <v-spacer></v-spacer>
              <v-btn 
                v-if="cartItems.length > 0"
                color="error" 
                text 
                @click="clearCart"
              >
                <v-icon left>mdi-delete-sweep</v-icon>
                清空购物车
              </v-btn>
            </v-card-title>
            <v-card-text>
              <v-container v-if="loading">
                <v-row justify="center">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </v-row>
              </v-container>
              
              <v-container v-else-if="cartItems.length > 0">
                <!-- 全选 -->
                <v-row class="mb-4">
                  <v-col>
                    <v-checkbox
                      v-model="selectAll"
                      label="全选"
                      @change="toggleSelectAll"
                      hide-details
                    ></v-checkbox>
                  </v-col>
                </v-row>
                
                <!-- 购物车列表 -->
                <v-row v-for="item in cartItems" :key="item.id" class="cart-item mb-4">
                  <v-col cols="1" class="d-flex align-center">
                    <v-checkbox
                      v-model="item.selected"
                      hide-details
                      @change="updateSelectAll"
                    ></v-checkbox>
                  </v-col>
                  <v-col cols="2">
                    <v-img
                      :src="getImageUrl(item.product)"
                      aspect-ratio="1"
                      class="grey lighten-2"
                      max-height="120"
                      contain
                    ></v-img>
                  </v-col>
                  <v-col cols="4">
                    <div class="font-weight-bold mb-2">{{ item.product.name }}</div>
                    <div class="text-body-2 grey--text">{{ item.product.description }}</div>
                    <div class="mt-2">
                      <v-chip small color="primary" text-color="white">
                        ¥{{ item.product.price.toFixed(2) }}
                      </v-chip>
                      <v-chip small class="ml-2" :color="item.product.stock > 0 ? 'success' : 'error'" text-color="white">
                        库存: {{ item.product.stock }}
                      </v-chip>
                    </div>
                  </v-col>
                  <v-col cols="2" class="d-flex align-center">
                    <v-btn icon small @click="decreaseQuantity(item)" :disabled="item.quantity <= 1">
                      <v-icon>mdi-minus</v-icon>
                    </v-btn>
                    <v-text-field
                      v-model.number="item.quantity"
                      type="number"
                      min="1"
                      :max="item.product.stock"
                      dense
                      hide-details
                      class="mx-2"
                      style="max-width: 60px"
                      @change="updateQuantity(item)"
                    ></v-text-field>
                    <v-btn icon small @click="increaseQuantity(item)" :disabled="item.quantity >= item.product.stock">
                      <v-icon>mdi-plus</v-icon>
                    </v-btn>
                  </v-col>
                  <v-col cols="2" class="d-flex align-center">
                    <span class="red--text font-weight-bold">
                      ¥{{ (item.product.price * item.quantity).toFixed(2) }}
                    </span>
                  </v-col>
                  <v-col cols="1" class="d-flex align-center">
                    <v-btn icon color="error" @click="removeFromCart(item)">
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </v-col>
                </v-row>
                
                <!-- 底部操作栏 -->
                <v-divider class="my-4"></v-divider>
                <v-row align="center">
                  <v-col cols="4">
                    <span class="text-body-1">
                      已选 <strong>{{ selectedCount }}</strong> 件商品
                    </span>
                  </v-col>
                  <v-col cols="4" class="text-center">
                    <span class="text-h6">
                      合计: <strong class="red--text">¥{{ selectedTotal.toFixed(2) }}</strong>
                    </span>
                  </v-col>
                  <v-col cols="4" class="text-right">
                    <v-btn 
                      color="warning" 
                      class="mr-2"
                      :disabled="selectedCount === 0"
                      @click="moveToFavorites"
                    >
                      <v-icon left>mdi-heart</v-icon>
                      转为收藏
                    </v-btn>
                    <v-btn 
                      color="success" 
                      :disabled="selectedCount === 0"
                      @click="goToCheckout"
                    >
                      <v-icon left>mdi-cart-check</v-icon>
                      去结算
                    </v-btn>
                  </v-col>
                </v-row>
              </v-container>
              
              <v-container v-else>
                <v-alert type="info">
                  购物车为空，快去挑选商品吧！
                  <template v-slot:append>
                    <v-btn to="/" color="primary" text>去购物</v-btn>
                  </template>
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
    
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
  name: 'ShoppingCart',
  data() {
    return {
      cartItems: [],
      loading: false,
      selectAll: false,
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  computed: {
    selectedItems() {
      return this.cartItems.filter(item => item.selected)
    },
    selectedCount() {
      return this.selectedItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    selectedTotal() {
      return this.selectedItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
    }
  },
  beforeMount() {
    if (!this.$validateCustomerAuth || !this.$validateCustomerAuth()) {
      this.$router.push('/customer/login')
      return
    }
  },
  mounted() {
    this.fetchCart()
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
    
    fetchCart() {
      this.loading = true
      this.$http.get('/api/cart')
        .then(response => {
          this.cartItems = response.data.map(item => ({
            ...item,
            selected: false
          }))
          this.loading = false
        })
        .catch(error => {
          console.error('获取购物车失败:', error)
          this.showSnackbar('获取购物车失败', 'error')
          this.loading = false
        })
    },
    
    toggleSelectAll() {
      this.cartItems.forEach(item => {
        item.selected = this.selectAll
      })
    },
    
    updateSelectAll() {
      this.selectAll = this.cartItems.length > 0 && this.cartItems.every(item => item.selected)
    },
    
    decreaseQuantity(item) {
      if (item.quantity > 1) {
        item.quantity--
        this.updateQuantity(item)
      }
    },
    
    increaseQuantity(item) {
      if (item.quantity < item.product.stock) {
        item.quantity++
        this.updateQuantity(item)
      }
    },
    
    updateQuantity(item) {
      if (item.quantity < 1) {
        item.quantity = 1
      }
      if (item.quantity > item.product.stock) {
        item.quantity = item.product.stock
        this.showSnackbar('数量不能超过库存', 'error')
      }
      
      this.$http.put(`/api/cart/${item.id}?quantity=${item.quantity}`)
        .then(() => {
          // 数量更新成功
        })
        .catch(error => {
          console.error('更新数量失败:', error)
          this.showSnackbar(error.response?.data || '更新数量失败', 'error')
          this.fetchCart()
        })
    },
    
    removeFromCart(item) {
      if (confirm('确定要从购物车中移除该商品吗？')) {
        this.$http.delete(`/api/cart/${item.id}`)
          .then(() => {
            this.showSnackbar('已从购物车移除')
            this.fetchCart()
          })
          .catch(error => {
            console.error('移除失败:', error)
            this.showSnackbar('移除失败', 'error')
          })
      }
    },
    
    clearCart() {
      if (confirm('确定要清空购物车吗？')) {
        this.$http.delete('/api/cart/clear')
          .then(() => {
            this.showSnackbar('购物车已清空')
            this.cartItems = []
          })
          .catch(error => {
            console.error('清空失败:', error)
            this.showSnackbar('清空失败', 'error')
          })
      }
    },
    
    moveToFavorites() {
      const selectedIds = this.selectedItems.map(item => item.id)
      if (selectedIds.length === 0) {
        this.showSnackbar('请先选择商品', 'error')
        return
      }
      
      if (confirm(`确定要将选中的 ${selectedIds.length} 件商品转为收藏吗？`)) {
        this.$http.post('/api/cart/move-to-favorites', { cartIds: selectedIds })
          .then(response => {
            this.showSnackbar(response.data.message || '转移成功')
            this.fetchCart()
          })
          .catch(error => {
            console.error('转移失败:', error)
            this.showSnackbar(error.response?.data || '转移失败', 'error')
          })
      }
    },
    
    goToCheckout() {
      const selectedIds = this.selectedItems.map(item => item.id)
      if (selectedIds.length === 0) {
        this.showSnackbar('请先选择商品', 'error')
        return
      }
      
      // 将选中的购物车ID存储到sessionStorage，传递给结算页面
      sessionStorage.setItem('checkoutCartIds', JSON.stringify(selectedIds))
      this.$router.push('/customer/checkout')
    }
  }
}
</script>

<style scoped>
.shopping-cart {
  padding: 20px 0;
}

.cart-item {
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}

.cart-item:last-child {
  border-bottom: none;
}
</style>
