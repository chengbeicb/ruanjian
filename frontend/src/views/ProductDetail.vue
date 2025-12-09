<template>
  <div class="product-detail">
    <v-container fluid>
      <v-layout align-center justify-center>
        <v-flex xs12 sm8 md6>
          <v-card v-if="product">
            <!-- 多图片显示组件 -->
            <v-container class="mb-4">
              <v-layout row>
                <v-flex xs2>
                  <!-- 缩略图列表 -->
                  <v-layout column>
                    <v-avatar
                      v-for="(img, index) in product.imageUrls"
                      :key="index"
                      :src="img"
                      size="80"
                      class="mb-2 cursor-pointer"
                      :class="{ 'border-2 border-primary': currentImageIndex === index }"
                      @click="currentImageIndex = index"
                    ></v-avatar>
                  </v-layout>
                </v-flex>
                <v-flex xs10>
                  <!-- 主图显示 -->
                  <v-img
                    :src="product.imageUrls[currentImageIndex] || 'https://via.placeholder.com/800x450?text=暂无图片'"
                    aspect-ratio="16/9"
                    class="grey lighten-2"
                  ></v-img>
                </v-flex>
              </v-layout>
            </v-container>
            <v-card-title>{{ product.name }}</v-card-title>
            <v-card-subtitle>价格: ¥{{ product.price.toFixed(2) }}</v-card-subtitle>
            <v-card-text>{{ product.description }}</v-card-text>
            <v-card-text>
              <v-chip :color="product.available ? 'success' : 'error'" text-color="white" class="mr-2">
                {{ product.available ? '可购买' : '已售出' }}
              </v-chip>
              <v-chip v-if="product.frozen" color="warning" text-color="white">
                有人正在购买
              </v-chip>
              <v-chip v-if="product.stock" class="ml-2" color="info" text-color="white">
                库存: {{ product.stock }}
              </v-chip>
            </v-card-text>
            
            <!-- 购买数量选择 -->
            <v-card-text v-if="isCustomerLoggedIn && product.available && !product.frozen">
              <v-row align="center">
                <v-col cols="auto">
                  <span class="text-body-1">购买数量:</span>
                </v-col>
                <v-col cols="auto">
                  <v-btn icon small @click="decreaseQuantity" :disabled="quantity <= 1">
                    <v-icon>mdi-minus</v-icon>
                  </v-btn>
                  <v-text-field
                    v-model.number="quantity"
                    type="number"
                    min="1"
                    :max="product.stock || 99"
                    dense
                    hide-details
                    class="mx-2 d-inline-block"
                    style="max-width: 60px"
                  ></v-text-field>
                  <v-btn icon small @click="increaseQuantity" :disabled="quantity >= (product.stock || 99)">
                    <v-icon>mdi-plus</v-icon>
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-text>
            
            <v-card-actions>
              <!-- 收藏按钮 -->
              <v-btn
                v-if="isCustomerLoggedIn"
                :color="isFavorited ? 'red' : 'grey'"
                text
                @click="toggleFavorite"
                :loading="favoriteLoading"
              >
                <v-icon left>{{ isFavorited ? 'mdi-heart' : 'mdi-heart-outline' }}</v-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </v-btn>
              
              <v-spacer></v-spacer>
              
              <!-- 加入购物车按钮 -->
              <v-btn
                v-if="isCustomerLoggedIn"
                :disabled="!product.available || product.frozen"
                color="warning"
                @click="addToCart"
                :loading="cartLoading"
                class="mr-2"
              >
                <v-icon left>mdi-cart-plus</v-icon>
                加入购物车
              </v-btn>
              
              <!-- 直接购买按钮 -->
              <v-btn
                :disabled="!product.available || product.frozen"
                color="success"
                @click="goToPurchase"
              >
                {{ product.frozen ? '有人正在购买' : '立即购买' }}
              </v-btn>
              <v-btn color="primary" @click="goBack">返回</v-btn>
            </v-card-actions>
          </v-card>
          <v-card v-else>
            <v-card-text>
              <v-alert type="error" dismissible>
                商品不存在
              </v-alert>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="goBack">返回</v-btn>
            </v-card-actions>
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
  name: 'ProductDetail',
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      product: null,
      currentImageIndex: 0,
      quantity: 1,
      isFavorited: false,
      favoriteLoading: false,
      cartLoading: false,
      snackbar: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  computed: {
    isCustomerLoggedIn() {
      return this.$validateCustomerAuth ? this.$validateCustomerAuth() : false
    }
  },
  mounted() {
    this.fetchProduct()
    if (this.isCustomerLoggedIn) {
      this.checkFavoriteStatus()
    }
  },
  methods: {
    showSnackbar(message, type = 'success') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    fetchProduct() {
      this.$http.get(`/api/products/${this.id}`)
        .then(response => {
          const productData = response.data;
          this.product = {
            ...productData,
            available: productData.status === 'AVAILABLE',
            frozen: productData.status === 'FROZEN',
            imageUrls: (productData.imageUrls ? productData.imageUrls.split(',') : ['']).map(url => {
              if (url && url.startsWith('file:///')) {
                const filename = url.split(/[\/]/).pop();
                return `http://localhost:8080/images/${filename}`;
              }
              return url || 'https://via.placeholder.com/800x450?text=暂无图片';
            })
          };
          if (!this.product.imageUrls[0]) {
             this.product.imageUrls[0] = 'https://via.placeholder.com/800x450?text=暂无图片';
          }
          this.currentImageIndex = 0;
        })
        .catch(error => {
          console.error('获取商品详情失败:', error);
          this.showSnackbar('获取商品详情失败', 'error');
        });
    },
    
    checkFavoriteStatus() {
      this.$http.get(`/api/favorites/check/${this.id}`)
        .then(response => {
          this.isFavorited = response.data.favorited
        })
        .catch(error => {
          console.error('检查收藏状态失败:', error)
        })
    },
    
    toggleFavorite() {
      if (!this.isCustomerLoggedIn) {
        this.showSnackbar('请先登录', 'error')
        return
      }
      
      this.favoriteLoading = true
      
      if (this.isFavorited) {
        // 取消收藏
        this.$http.delete(`/api/favorites/${this.id}`)
          .then(() => {
            this.isFavorited = false
            this.showSnackbar('已取消收藏')
            this.favoriteLoading = false
          })
          .catch(error => {
            console.error('取消收藏失败:', error)
            this.showSnackbar('取消收藏失败', 'error')
            this.favoriteLoading = false
          })
      } else {
        // 添加收藏
        this.$http.post(`/api/favorites/${this.id}`)
          .then(() => {
            this.isFavorited = true
            this.showSnackbar('收藏成功')
            this.favoriteLoading = false
          })
          .catch(error => {
            console.error('收藏失败:', error)
            this.showSnackbar(error.response?.data || '收藏失败', 'error')
            this.favoriteLoading = false
          })
      }
    },
    
    decreaseQuantity() {
      if (this.quantity > 1) {
        this.quantity--
      }
    },
    
    increaseQuantity() {
      if (this.quantity < (this.product.stock || 99)) {
        this.quantity++
      }
    },
    
    addToCart() {
      if (!this.isCustomerLoggedIn) {
        this.showSnackbar('请先登录', 'error')
        return
      }
      
      if (this.quantity < 1) {
        this.showSnackbar('数量必须大于0', 'error')
        return
      }
      
      this.cartLoading = true
      
      this.$http.post('/api/cart', {
        productId: parseInt(this.id),
        quantity: this.quantity
      })
        .then(() => {
          this.showSnackbar('已加入购物车')
          this.cartLoading = false
        })
        .catch(error => {
          console.error('加入购物车失败:', error)
          this.showSnackbar(error.response?.data || '加入购物车失败', 'error')
          this.cartLoading = false
        })
    },
    
    goToPurchase() {
      // 使用浏览器内置的确认框，确认后才跳转到购买表单
      if (window.confirm('您确定要购买此商品吗？')) {
        this.$router.push({ name: 'purchase-form', params: { id: this.id } });
      }
    },
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.product-detail {
  padding: 20px 0;
}
</style>