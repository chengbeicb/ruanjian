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
              <v-text-field
                v-model="productForm.description"
                label="商品描述"
                required
                multiline
                rows="3"
                :rules="[v => !!v || '请输入商品描述']"
              ></v-text-field>
              
              <!-- 修改图片上传部分 -->
              <div v-if="!editingProduct || !productForm.imageUrl" class="mb-4">
                <v-file-input
                  v-model="selectedFile"
                  label="选择本地图片"
                  accept="image/*"
                  @change="handleFileChange"
                ></v-file-input>
              </div>
              
              <div v-if="productForm.imageUrl" class="mb-4">
                <v-img
                  :src="productForm.imageUrl"
                  aspect-ratio="16/9"
                  class="grey lighten-2 mb-2"
                ></v-img>
                <v-btn @click="clearImage" color="error" text>清除图片</v-btn>
              </div>
              
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
        imageUrl: '',
        price: ''
      },
      selectedFile: null, // 新增：用于存储选择的文件
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
  mounted() {
    this.fetchProducts()
  },
  methods: {
    // 处理文件选择变化
    handleFileChange(file) {
      if (file) {
        // 创建FormData对象
        const formData = new FormData()
        formData.append('file', file)
        
        // 上传文件到后端
        this.$http.post('/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        .then(response => {
          // 保存返回的图片URL
          this.productForm.imageUrl = response.data
        })
        .catch(error => {
          console.error('文件上传失败:', error)
          this.showSnackbar('图片上传失败', 'error')
        })
      }
    },
    
    // 清除已选择的图片
    clearImage() {
      this.productForm.imageUrl = ''
      this.selectedFile = null
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
        imageUrl: this.productForm.imageUrl,
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
            if (product.imageUrl) {
              // 如果已经是完整URL，保持不变
              if (product.imageUrl.startsWith('http')) {
                // 已经是完整URL，无需修改
              } 
              // 添加对file://格式URL的处理
              else if (product.imageUrl.startsWith('file://')) {
                // 提取文件名
                const fileName = product.imageUrl.split('\\').pop().split('/').pop();
                // 构建正确的http URL
                product.imageUrl = 'http://localhost:8080/picture/' + fileName;
              }
              // 如果是相对路径，检查是否以/uploads/开头
              else if (product.imageUrl.startsWith('/uploads/')) {
                // 添加正确的基础路径前缀
                product.imageUrl = 'http://localhost:8080' + product.imageUrl;
              }
              // 否则，假设是项目根目录下的picture目录中的图片
              else {
                // 直接使用项目中的picture目录
                product.imageUrl = 'http://localhost:8080/picture/' + product.imageUrl;
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
        imageUrl: product.imageUrl,
        price: product.price.toString()
      }
      this.showAddDialog = true
    },
    cancelAdd() {
      this.showAddDialog = false
      this.editingProduct = null
      this.productForm = {
        name: '',
        description: '',
        imageUrl: '',
        price: ''
      }
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