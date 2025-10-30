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
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                :disabled="!product.available || product.frozen"
                color="success"
                @click="goToPurchase"
              >
                {{ product.frozen ? '有人正在购买' : '我要购买' }}
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
      currentImageIndex: 0
    }
  },
  mounted() {
    this.fetchProduct()
  },
  methods: {
    fetchProduct() {
      this.$http.get(`/products/${this.id}`)
        .then(response => {
          // 添加数据转换逻辑
          const productData = response.data
          this.product = {
            ...productData,
            available: productData.status === 'AVAILABLE',
            frozen: productData.status === 'FROZEN',
            // 处理多图片，转换为数组
            imageUrls: productData.imageUrls ? productData.imageUrls.split(',') : ['']
          }
          this.currentImageIndex = 0
        })
        .catch(error => {
          console.error('获取商品详情失败:', error)
          this.$snackbar.open({
            message: '获取商品详情失败',
            type: 'error'
          })
        })
    },
    goToPurchase() {
      this.$router.push({ name: 'purchase-form', params: { id: this.id } })
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