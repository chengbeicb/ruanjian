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
              <!-- 搜索和筛选表单 -->
              <v-form @submit.prevent="searchProducts">
                <v-layout row wrap>
                  <v-flex xs12 sm6>
                    <v-text-field
                      v-model="searchForm.keyword"
                      label="搜索关键词"
                      placeholder="商品名称或描述"
                    ></v-text-field>
                  </v-flex>
                  <v-flex xs12 sm3>
                    <v-select
                      v-model="searchForm.categoryLevel1"
                      label="一级分类"
                      :items="categoryLevel1Options"
                      item-text="name"
                      item-value="value"
                      placeholder="选择分类"
                    ></v-select>
                  </v-flex>
                  <v-flex xs12 sm3>
                    <v-select
                      v-model="searchForm.categoryLevel2"
                      label="二级分类"
                      :items="categoryLevel2Options"
                      item-text="name"
                      item-value="value"
                      placeholder="选择分类"
                      :disabled="!searchForm.categoryLevel1"
                    ></v-select>
                  </v-flex>
                  <v-flex xs12 class="text-right">
                    <v-btn type="submit" color="primary" class="mr-2">搜索</v-btn>
                    <v-btn @click="resetSearch" color="secondary">重置</v-btn>
                  </v-flex>
                </v-layout>
              </v-form>
            </v-card-text>
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
      searchForm: {
        keyword: '',
        categoryLevel1: null,
        categoryLevel2: null
      },
      categoryLevel1Options: [
        { name: '电子产品', value: '电子产品' },
        { name: '服装鞋帽', value: '服装鞋帽' },
        { name: '家居生活', value: '家居生活' },
        { name: '食品饮料', value: '食品饮料' },
        { name: '图书文具', value: '图书文具' }
      ],
      categoryLevel2Options: [],
      categoryMap: {
        '电子产品': [
          { name: '手机', value: '手机' },
          { name: '电脑', value: '电脑' },
          { name: '家电', value: '家电' },
          { name: '配件', value: '配件' }
        ],
        '服装鞋帽': [
          { name: '男装', value: '男装' },
          { name: '女装', value: '女装' },
          { name: '鞋子', value: '鞋子' },
          { name: '配饰', value: '配饰' }
        ],
        '家居生活': [
          { name: '家具', value: '家具' },
          { name: '厨具', value: '厨具' },
          { name: '床上用品', value: '床上用品' },
          { name: '收纳整理', value: '收纳整理' }
        ],
        '食品饮料': [
          { name: '零食', value: '零食' },
          { name: '饮料', value: '饮料' },
          { name: '生鲜', value: '生鲜' },
          { name: '粮油', value: '粮油' }
        ],
        '图书文具': [
          { name: '图书', value: '图书' },
          { name: '文具', value: '文具' },
          { name: '办公用品', value: '办公用品' },
          { name: '体育用品', value: '体育用品' }
        ]
      },
      snackbar: {
        show: false,
        message: '',
        type: ''
      }
    }
  },
  watch: {
    'searchForm.categoryLevel1'(newValue) {
      // 当一级分类改变时，更新二级分类选项
      this.searchForm.categoryLevel2 = null;
      if (newValue) {
        this.categoryLevel2Options = this.categoryMap[newValue.value] || [];
      } else {
        this.categoryLevel2Options = [];
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
            frozen: product.status === 'FROZEN',
            // 处理多图片，取第一张作为列表显示
            imageUrl: product.imageUrls ? product.imageUrls.split(',')[0] : ''
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
    searchProducts() {
      const params = {
        keyword: this.searchForm.keyword || undefined,
        categoryLevel1: this.searchForm.categoryLevel1 || undefined,
        categoryLevel2: this.searchForm.categoryLevel2 || undefined
      }
      
      this.$http.get('/products', { params })
        .then(response => {
          this.products = response.data.map(product => ({
            ...product,
            available: product.status === 'AVAILABLE',
            frozen: product.status === 'FROZEN',
            imageUrl: product.imageUrls ? product.imageUrls.split(',')[0] : ''
          }))
        })
        .catch(error => {
          console.error('搜索商品失败:', error)
          this.snackbar = {
            show: true,
            message: '搜索商品失败',
            type: 'error'
          }
        })
    },
    resetSearch() {
      this.searchForm = {
        keyword: '',
        categoryLevel1: null,
        categoryLevel2: null
      }
      this.categoryLevel2Options = []
      this.fetchProducts()
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