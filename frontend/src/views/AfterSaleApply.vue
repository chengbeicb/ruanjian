<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title>
            <v-btn icon @click="$router.go(-1)">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            申请售后服务
          </v-card-title>

          <v-card-text>
            <v-form ref="form" v-model="valid">
              <v-select
                v-model="afterSale.serviceType"
                :items="serviceTypes"
                label="服务类型"
                :rules="[rules.required]"
                required
              ></v-select>

              <v-select
                v-model="afterSale.reason"
                :items="reasons"
                label="申请原因"
                :rules="[rules.required]"
                required
              ></v-select>

              <v-textarea
                v-model="afterSale.description"
                label="问题描述"
                placeholder="请详细描述您遇到的问题..."
                rows="5"
                counter
              ></v-textarea>

              <v-file-input
                v-model="images"
                label="上传图片（选填）"
                multiple
                accept="image/*"
                prepend-icon="mdi-camera"
                hint="最多上传5张图片"
                persistent-hint
                show-size
                @change="handleImageUpload"
              ></v-file-input>

              <v-row v-if="imageUrls.length > 0" class="mt-2">
                <v-col cols="4" v-for="(url, index) in imageUrls" :key="index">
                  <v-card outlined>
                    <v-img :src="url" aspect-ratio="1" contain></v-img>
                    <v-card-actions>
                      <v-spacer></v-spacer>
                      <v-btn icon small @click="removeImage(index)">
                        <v-icon small>mdi-close</v-icon>
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>

          <v-card-actions class="pa-4">
            <v-btn large text @click="$router.go(-1)">取消</v-btn>
            <v-spacer></v-spacer>
            <v-btn 
              large 
              color="primary" 
              @click="submitAfterSale"
              :loading="submitting"
              :disabled="!valid"
            >
              提交申请
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <snackbar-notification ref="snackbar"></snackbar-notification>
  </v-container>
</template>

<script>
import axios from 'axios';
import SnackbarNotification from '@/components/SnackbarNotification.vue';

export default {
  name: 'AfterSaleApply',
  components: {
    SnackbarNotification
  },
  data() {
    return {
      valid: false,
      submitting: false,
      afterSale: {
        orderItemId: null,
        serviceType: '',
        reason: '',
        description: ''
      },
      images: [],
      imageUrls: [],
      serviceTypes: [
        { text: '退货退款', value: 'RETURN' },
        { text: '仅退款', value: 'REFUND' },
        { text: '换货', value: 'EXCHANGE' }
      ],
      reasons: [
        '质量问题',
        '商品描述不符',
        '收到商品损坏',
        '商品缺件',
        '不想要了',
        '其他原因'
      ],
      rules: {
        required: value => !!value || '必填项'
      }
    };
  },
  mounted() {
    this.afterSale.orderItemId = this.$route.params.orderItemId;
  },
  methods: {
    handleImageUpload(files) {
      if (files.length > 5) {
        this.$refs.snackbar.show('最多只能上传5张图片', 'warning');
        this.images = files.slice(0, 5);
      }
      
      // 模拟图片上传，生成预览URL
      this.imageUrls = [];
      this.images.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imageUrls.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
    },
    removeImage(index) {
      this.images.splice(index, 1);
      this.imageUrls.splice(index, 1);
    },
    async submitAfterSale() {
      if (this.$refs.form.validate()) {
        this.submitting = true;
        try {
          const data = {
            ...this.afterSale,
            imageUrls: this.imageUrls
          };
          
          await axios.post('/api/after-sales', data);
          this.$refs.snackbar.show('售后申请提交成功', 'success');
          
          setTimeout(() => {
            this.$router.push('/after-sales');
          }, 1500);
        } catch (error) {
          this.$refs.snackbar.show(error.response?.data?.message || '提交失败', 'error');
        } finally {
          this.submitting = false;
        }
      }
    }
  }
};
</script>
