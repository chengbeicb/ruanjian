<template>
  <div class="product-management">
    <v-container fluid>
      <v-toolbar color="primary" dark>
        <v-toolbar-title>商品管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn @click="showAddDialog = true" color="success" text>+ 添加商品</v-btn>
          <v-btn to="/seller/dashboard" text>返回仪表盘</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      
      <!-- 添加/编辑商品对话框 -->
      <v-dialog v-model="showAddDialog" max-width="600px">
        <v-card>
          <v-card-title>{{ editingProduct ? '编辑商品' : '添加商品' }}</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="saveProduct" ref="productForm">
              <v-text-field
                v-model="productForm.name"
                label="商品名称"
                required
                :rules="[v => !!v || '请输入商品名称']"
              ></v-text-field>
              <!-- 富文本编辑器 -->
              <v-textarea
                v-model="productForm.description"
                label="商品描述（支持富文本）"
                required
                rows="5"
                :rules="[v => !!v || '请输入商品描述']"
              ></v-textarea>
              <small class="text-secondary">可以输入HTML标签实现富文本效果</small>
              
              <!-- 多图片上传 -->
              <div class="mb-4">
                <v-file-input
                  v-model="selectedFiles"
                  label="选择图片（可多选）"
                  accept="image/*"
                  multiple
                  @change="handleFilesChange"
                ></v-file-input>
              </div>
              
              <!-- 已上传图片预览 -->
              <div v-if="productForm.imageUrls && productForm.imageUrls.length > 0" class="mb-4">
                <h4>已上传图片</h4>
                <v-layout row wrap>
                  <v-flex xs3 md2 v-for="(url, index) in productForm.imageUrls" :key="index">
                    <div class="relative">
                      <v-img
                        :src="url"
                        aspect-ratio="1/1"
                        class="grey lighten-2 mb-2"
                      ></v-img>
                      <v-btn
                        absolute
                        top="0"
                        right="0"
                        icon
                        color="error"
                        small
                        @click="removeImage(index)"
                      >
                        <v-icon>mdi-close</v-icon>
                      </v-btn>
                    </div>
                  </v-flex>
                </v-layout>
              </div>
              
              <!-- 分类选择 -->
              <v-layout row wrap>
                <v-flex xs12 sm6 class="mb-2">
                  <v-select
                    v-model="productForm.categoryLevel1"
                    label="一级分类"
                    :items="categoryLevel1Options"
                    item-text="name"
                    item-value="value"
                    required
                    :rules="[v => !!v || '请选择一级分类']"
                  ></v-select>
                </v-flex>
                <v-flex xs12 sm6 class="mb-2">
                  <v-select
                    v-model="productForm.categoryLevel2"
                    label="二级分类"
                    :items="categoryLevel2Options"
                    item-text="name"
                    item-value="value"
                    required
                    :rules="[v => !!v || '请选择二级分类']"
                    :disabled="!productForm.categoryLevel1"
                  ></v-select>
                </v-flex>
              </v-layout>
              
              <!-- 库存数量 -->
              <v-text-field
                v-model="productForm.stockQuantity"
                label="库存数量"
                type="number"
                required
                :rules="[
                  v => !!v || '请输入库存数量',
                  v => !isNaN(Number(v)) && Number(v) >= 0 || '请输入有效的库存数量'
                ]"
              ></v-text-field>
              
              <v-text-field
                v-model="productForm.price"
                label="商品价格"
                type="number"
                required
                :rules="[
                  v => !!v || '请输入商品价格',
                  v => !isNaN(Number(v)) && Number(v) > 0 || '请输入有效的价格'
                ]"
              ></v-text-field>
              <v-card-actions>
                <v-btn type="submit" color="primary" :loading="saving">
                  {{ editingProduct ? '更新' : '保存' }}
                </v-btn>
                <v-btn @click="cancelAdd" color="secondary">取消</v-btn>
              </v-card-actions>
            </v-form>
          </v-card-text>
        </v-card>
      </v-dialog>
      
      <!-- 商品列表 -->
      <v-layout mt-6>
        <v-flex xs12>
          <v-card>
            <v-card-title>
              <h3>我的商品</h3>
            </v-card-title>
            <v-card-text>
              <v-container v-if="products.length > 0">
                <v-layout wrap>
                  <v-flex xs12 v-for="product in products" :key="product.id">
                    <v-card hover class="mb-4">
                      <v-img
                        :src="product.imageUrl || 'https://via.placeholder.com/800x450?text=暂无图片'"
                        aspect-ratio="16/9"
                        class="grey lighten-2"
                      ></v-img>
                      <v-card-title>{{ product.name }}</v-card-title>
                      <v-card-text>{{ product.description }}</v-card-text>
                      <!-- 在商品卡片中更新状态显示部分（找到原有的v-chip标签） -->
                      <v-card-text>
                        <!-- 将原来的这行 -->
                        <!-- <v-chip :color="product.available ? 'success' : 'error'" text-color="white" class="mr-2"> -->
                        <!-- {{ product.available ? '可购买' : '已售出' }} -->
                        <!-- </v-chip> -->
                        
                        <!-- 替换为 -->
                        <v-chip :color="getProductStatusColor(product)" text-color="white" class="mr-2">
                          {{ product.statusText }}
                        </v-chip>
                        
                        <!-- 其他代码保持不变 -->
                        <v-chip v-if="product.frozen" color="warning" text-color="white">
                          已冻结
                        </v-chip>
                        <v-chip color="primary" text-color="white" class="ml-2">
                          ¥{{ product.price.toFixed(2) }}
                        </v-chip>
                      </v-card-text>
                      <v-card-actions>
                        <v-btn @click="editProduct(product)" color="primary" v-if="product.available">编辑</v-btn>
                        <v-btn 
                          @click="toggleProductStatus(product)" 
                          :color="product.available ? 'error' : 'success'"
                          v-if="!product.frozen"
                        >
                          {{ product.available ? '下架' : '上架' }}
                        </v-btn>
                        <!-- 移除冻结/解冻按钮 -->
                        <v-spacer></v-spacer>
                      </v-card-actions>
                    </v-card>
                  </v-flex>
                </v-layout>
              </v-container>
              <v-container v-else>
                <v-alert type="info" dismissible>
                  暂无商品，请添加新商品
                </v-alert>
              </v-container>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
      
      <!-- 添加Snackbar组件 -->
      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.type"
        :timeout="4000"
        bottom
        right
      >
        {{ snackbar.message }}
        <v-btn color="white" text @click="snackbar.show = false">关闭</v-btn>
      </v-snackbar>
      
      <!-- 商品列表代码保持不变 -->
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'ProductManagement',
  data() {
    return {
      showAddDialog: false,
      editingProduct: null,
      productForm: {
        name: '',
        description: '',
        imageUrls: [],
        categoryLevel1: '',
        categoryLevel2: '',
        stockQuantity: 0,
        price: ''
      },
      selectedFiles: [], // 用于多图片上传
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
      saving: false,
      products: [],
      // 修复这里 - 将snackbar从布尔值改为对象
      snackbar: {
        show: false,
        message: '',
        type: 'info'
      },
      // 可以移除未使用的属性
      // snackbarText: '',
      // snackbarColor: ''
    }
  },
  watch: {
    'productForm.categoryLevel1'(newValue) {
      // 当一级分类改变时，更新二级分类选项
      this.productForm.categoryLevel2 = '';
      if (newValue) {
        this.categoryLevel2Options = this.categoryMap[newValue] || [];
      } else {
        this.categoryLevel2Options = [];
      }
    }
  },
  mounted() {
    this.fetchProducts()
  },
  methods: {
    // 处理多文件选择变化
    handleFilesChange(files) {
      if (files && files.length > 0) {
        const uploadPromises = files.map(file => {
          const formData = new FormData()
          formData.append('file', file)
          
          return this.$http.post('/files/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }).then(response => {
            return response.data.url
          })
        })
        
        Promise.all(uploadPromises)
          .then(urls => {
            this.productForm.imageUrls = [...this.productForm.imageUrls, ...urls]
          })
          .catch(error => {
            console.error('上传文件失败:', error)
            this.showSnackbar('图片上传失败', 'error')
          })
      }
    },
    removeImage(index) {
      this.productForm.imageUrls.splice(index, 1)
    },
    
    // 保存商品方法（保持大部分不变）
    saveProduct() {
      // 表单验证
      if (!this.$refs.productForm.validate()) {
        return
      }
      
      this.saving = true
      const productData = {
        name: this.productForm.name,
        description: this.productForm.description,
        imageUrls: this.productForm.imageUrls.join(','),
        categoryLevel1: this.productForm.categoryLevel1,
        categoryLevel2: this.productForm.categoryLevel2,
        stockQuantity: parseInt(this.productForm.stockQuantity),
        price: parseFloat(this.productForm.price)
      }
      
      if (this.editingProduct) {
        // 更新商品
        this.$http.put(`/products/${this.editingProduct.id}`, productData)
          .then(() => {
            this.saving = false
            this.fetchProducts()
            this.cancelAdd()
            this.showSnackbar('商品更新成功', 'success')
          })
          .catch(error => {
            this.saving = false
            console.error('更新商品失败:', error)
            this.showSnackbar('更新商品失败', 'error')
          })
      } else {
        // 创建商品
        this.$http.post('/products', productData)
          .then(() => {
            this.saving = false
            this.fetchProducts()
            this.cancelAdd()
            this.showSnackbar('商品添加成功', 'success')
          })
          .catch(error => {
            this.saving = false
            console.error('添加商品失败:', error)
            this.showSnackbar('添加商品失败', 'error')
          })
      }
    },
    
    // 其他方法保持不变
    showSnackbar(message, type = 'info') {
      this.snackbar.message = message
      this.snackbar.type = type
      this.snackbar.show = true
    },
    
    // 在fetchProducts方法中添加数据转换逻辑
    fetchProducts() {
      this.$http.get('/products/seller')
        .then(response => {
          // 添加数据转换逻辑
          this.products = response.data.map(product => {
            // 根据status枚举值设置available和frozen属性
            product.available = product.status === 'AVAILABLE';
            product.frozen = product.status === 'FROZEN';
            // 添加对UNPUBLISHED状态的处理
            product.statusText = 
              product.status === 'AVAILABLE' ? '可购买' :
              product.status === 'SOLD' ? '已售出' :
              product.status === 'FROZEN' ? '已冻结' :
              product.status === 'UNPUBLISHED' ? '已下架' : '未知状态';
            
            // 修复图片显示问题 - 处理图片URL
            if (product.imageUrls) {
              // 处理多图片URL，取第一张作为预览图
              const imageUrls = product.imageUrls.split(',');
              product.imageUrl = imageUrls[0];
              
              // 处理URL格式
              if (product.imageUrl) {
                if (product.imageUrl.startsWith('http')) {
                  // 已经是完整URL，无需修改
                } 
                else if (product.imageUrl.startsWith('file://')) {
                  const fileName = product.imageUrl.split('\\').pop().split('/').pop();
                  product.imageUrl = 'http://localhost:8080/picture/' + fileName;
                }
                else if (product.imageUrl.startsWith('/uploads/')) {
                  product.imageUrl = 'http://localhost:8080' + product.imageUrl;
                }
                else {
                  product.imageUrl = 'http://localhost:8080/picture/' + product.imageUrl;
                }
              }
            }
            
            return product;
          });
        })
        .catch(error => {
          console.error('获取商品列表失败:', error)
          this.showSnackbar('获取商品列表失败', 'error')
        })
    },  // 添加这个逗号
    editProduct(product) {
      this.editingProduct = product
      this.productForm = {
        name: product.name,
        description: product.description,
        imageUrls: product.imageUrls ? product.imageUrls.split(',') : [],
        categoryLevel1: product.categoryLevel1 || '',
        categoryLevel2: product.categoryLevel2 || '',
        stockQuantity: product.stockQuantity || 0,
        price: product.price.toString()
      }
      // 更新二级分类选项
      if (product.categoryLevel1) {
        this.categoryLevel2Options = this.categoryMap[product.categoryLevel1] || [];
      }
      this.showAddDialog = true
    },
    cancelAdd() {
      this.showAddDialog = false
      this.editingProduct = null
      this.productForm = {
        name: '',
        description: '',
        imageUrls: [],
        categoryLevel1: '',
        categoryLevel2: '',
        stockQuantity: 0,
        price: ''
      }
      this.selectedFiles = []
      this.categoryLevel2Options = []
      this.$refs.productForm.reset()
    },
    toggleProductStatus(product) {
      const newStatus = !product.available
      // 调用正确的API端点，而不是不存在的/status端点
      const endpoint = newStatus ? `/products/${product.id}/publish` : `/products/${product.id}/unpublish`
      
      this.$http.put(endpoint)
        .then(response => {
          this.fetchProducts()
          this.showSnackbar(newStatus ? '商品已上架' : '商品已下架', 'success')
        })
        .catch(error => {
          console.error('更新商品状态失败:', error)
          this.showSnackbar('更新商品状态失败', 'error')
        })
    },
    // 移除toggleProductFrozen方法
    
    // 添加getStatusColor方法到现有的methods对象中
    getProductStatusColor(product) {
      switch(product.status) {
        case 'AVAILABLE': return 'success';
        case 'SOLD': return 'error';
        case 'FROZEN': return 'warning';
        case 'UNPUBLISHED': return 'secondary';
        default: return 'info';
      }
    } // 移除这个逗号，最后一个方法后不应该有逗号
  } // methods对象正确结束
} // Vue组件对象正确结束
</script>

<style scoped>
.product-management {
  padding: 20px 0;
}
</style>