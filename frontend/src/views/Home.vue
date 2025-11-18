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
                      <div class="image-container" style="position: relative; height: 200px;">
                        <!-- 如果图片加载失败，显示占位符 -->
                        <div v-if="product.imageError || !product.imageUrl" 
                             class="d-flex align-center justify-center fill-height grey lighten-3" 
                             style="height: 200px;">
                          <div class="text-center">
                            <v-icon size="64" color="grey">mdi-image-off</v-icon>
                            <div class="mt-2 grey--text">暂无图片</div>
                          </div>
                        </div>
                        <!-- 正常图片显示 -->
                        <v-img
                          v-else
                          :src="product.imageUrl"
                          aspect-ratio="16/9"
                          height="200"
                          class="grey lighten-2"
                          @error="onImageError($event, product)"
                          @load="onImageLoad(product)"
                          contain
                        >
                          <template v-slot:placeholder>
                            <v-row
                              class="fill-height ma-0"
                              align="center"
                              justify="center"
                            >
                              <div class="text-center">
                                <v-progress-circular
                                  indeterminate
                                  color="primary"
                                  size="40"
                                ></v-progress-circular>
                                <div class="mt-2 grey--text">加载中...</div>
                              </div>
                            </v-row>
                          </template>
                        </v-img>
                      </div>
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
      showDebugInfo: false, // 关闭调试信息
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
      // 当一级分类改变时，更新二级分类选项 (修复: newValue 已是选中的 value 字符串, 不需要 .value)
      this.searchForm.categoryLevel2 = null
      if (newValue) {
        this.categoryLevel2Options = this.categoryMap[newValue] || []
      } else {
        this.categoryLevel2Options = []
      }
    }
  },
  mounted() {
    this.fetchProducts()
  },
  methods: {
      showSnackbar(message, type = 'info') {
        this.snackbar = {
          show: true,
          message,
          type
        }
        setTimeout(() => {
          this.snackbar.show = false
        }, 3000)
      },
      onImageError(event, product) {
        console.error(`图片加载失败 - 产品ID: ${product.id}`, { 
          url: product.imageUrl, 
          error: event,
          naturalWidth: event.target?.naturalWidth,
          naturalHeight: event.target?.naturalHeight,
          status: event.target?.complete,
          src: event.target?.src
        });
        
        // 标记图片加载失败，显示占位符
        this.$set(product, 'imageError', true);
        
        // 尝试直接测试图片URL
        fetch(product.imageUrl)
          .then(response => {
            console.log(`Fetch测试 - 产品ID: ${product.id}, 状态: ${response.status}, URL: ${product.imageUrl}`);
            if (!response.ok) {
              console.error(`图片URL返回错误状态: ${response.status} ${response.statusText}`);
            }
          })
          .catch(error => {
            console.error(`Fetch测试失败 - 产品ID: ${product.id}:`, error);
          });
      },
      onImageLoad(product) {
        console.log(`图片加载成功 - 产品ID: ${product.id}`, product.imageUrl);
      },
      fetchProducts() {
        this.$http.get('/api/products', { params: { status: 'AVAILABLE' } })
          .then(response => {
            if (response.data && Array.isArray(response.data)) {
              console.log('原始商品数据:', response.data);
              this.products = response.data.map(p => {
                console.log('单个商品数据:', JSON.stringify(p, null, 2));
                
                // 默认占位图
                let imageUrl = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD48L3N2Zz4=';
                
                // 尝试所有可能的图片字段名
                let rawUrl = '';
                if (p.imageUrls && p.imageUrls !== null && typeof p.imageUrls === 'string') {
                  rawUrl = p.imageUrls.split(',')[0]; // 如果是多个图片，取第一个
                } else if (p.imageUrl && p.imageUrl !== null && typeof p.imageUrl === 'string') {
                  rawUrl = p.imageUrl;
                } else {
                  rawUrl = p.image || p.picUrl || p.pictureUrl || '';
                }
                
                console.log('尝试的字段:', {
                  imageUrl: p.imageUrl,
                  imageUrls: p.imageUrls, 
                  image: p.image,
                  picUrl: p.picUrl,
                  pictureUrl: p.pictureUrl
                });
                console.log('最终选中的原始图片URL:', rawUrl);

                if (rawUrl && rawUrl.trim() !== '') {
                  if (rawUrl.startsWith('file:///')) {
                    const filename = rawUrl.split(/[\\\/]/).pop();
                    console.log('从file://协议提取的文件名:', filename);
                    if (filename && filename.length > 0) {
                      // 使用相对路径，让Vue代理处理
                      imageUrl = `/images/${filename}`;
                      console.log('转换后的图片URL:', imageUrl);
                    }
                  } else if (rawUrl.startsWith('/images/')) {
                    imageUrl = rawUrl;
                  } else if (rawUrl.startsWith('/')) {
                    imageUrl = rawUrl;
                  } else if (rawUrl.startsWith('http://') || rawUrl.startsWith('https://')) {
                    imageUrl = rawUrl;
                  }
                }
                
                console.log('最终处理后的图片URL:', imageUrl);
                
                return {
                  id: p.id,
                  name: p.name,
                  description: p.description,
                  price: p.price,
                  status: p.status,
                  available: p.status === 'AVAILABLE',
                  frozen: p.status === 'FROZEN',
                  imageUrl: imageUrl,
                  imageError: false // 初始化图片错误状态
                };
              });
            } else {
              this.products = [];
              console.warn('获取到的商品数据格式不正确或为空:', response.data);
            }
          })
          .catch(error => {
            console.error('获取商品列表失败:', error);
            this.products = [];
            this.showSnackbar('获取商品列表失败', 'error');
          });
      },
      searchProducts() {
        const params = {
          keyword: this.searchForm.keyword || undefined,
          categoryLevel1: this.searchForm.categoryLevel1 || undefined,
          categoryLevel2: this.searchForm.categoryLevel2 || undefined
        };
        
        this.$http.get('/api/products', { params })
          .then(response => {
            if (response.data && Array.isArray(response.data)) {
              console.log('搜索商品数据:', response.data);
              this.products = response.data.map(p => {
                console.log('搜索单个商品数据:', JSON.stringify(p, null, 2));
                let imageUrl = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxOCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD48L3N2Zz4=';
                // 尝试所有可能的图片字段名
                let rawUrl = '';
                if (p.imageUrls && p.imageUrls !== null && typeof p.imageUrls === 'string') {
                  rawUrl = p.imageUrls.split(',')[0]; // 如果是多个图片，取第一个
                } else if (p.imageUrl && p.imageUrl !== null && typeof p.imageUrl === 'string') {
                  rawUrl = p.imageUrl;
                } else {
                  rawUrl = p.image || p.picUrl || p.pictureUrl || '';
                }
                
                console.log('搜索使用的原始图片URL:', rawUrl);

                if (rawUrl && rawUrl.trim() !== '') {
                  if (rawUrl.startsWith('file:///')) {
                    const filename = rawUrl.split(/[\\\/]/).pop();
                    if (filename && filename.length > 0) {
                      imageUrl = `http://localhost:8080/images/${filename}`;
                    }
                  } else if (rawUrl.startsWith('/images/')) {
                    imageUrl = `http://localhost:8080${rawUrl}`;
                  } else if (rawUrl.startsWith('/')) {
                    imageUrl = `http://localhost:8080${rawUrl}`;
                  } else if (rawUrl.startsWith('http://') || rawUrl.startsWith('https://')) {
                    imageUrl = rawUrl;
                  }
                }
                
                console.log('搜索处理后的图片URL:', imageUrl);

                return {
                  id: p.id,
                  name: p.name,
                  description: p.description,
                  price: p.price,
                  status: p.status,
                  available: p.status === 'AVAILABLE',
                  frozen: p.status === 'FROZEN',
                  imageUrl: imageUrl
                };
              });
            } else {
              this.products = [];
              console.warn('搜索到的商品数据格式不正确或为空:', response.data);
            }
          })
          .catch(error => {
            console.error('搜索商品失败:', error);
            this.products = [];
            this.showSnackbar('搜索商品失败', 'error');
          });
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