<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title>
            <span class="text-h5">我的售后</span>
          </v-card-title>

          <v-card-text>
            <v-row v-if="afterSales.length === 0">
              <v-col cols="12" class="text-center py-8">
                <v-icon size="64" color="grey lighten-1">mdi-package-variant</v-icon>
                <p class="text-h6 mt-4 grey--text">暂无售后申请</p>
              </v-col>
            </v-row>

            <v-row v-else>
              <v-col cols="12" v-for="afterSale in afterSales" :key="afterSale.id">
                <v-card outlined>
                  <v-card-title class="pb-2">
                    <span>售后单号：{{ afterSale.afterSaleNumber }}</span>
                    <v-spacer></v-spacer>
                    <v-chip :color="getStatusColor(afterSale.status)" small>
                      {{ getStatusText(afterSale.status) }}
                    </v-chip>
                  </v-card-title>

                  <v-divider></v-divider>

                  <v-card-text>
                    <v-row>
                      <v-col cols="12" md="8">
                        <p><strong>商品名称：</strong>{{ afterSale.product.name }}</p>
                        <p><strong>服务类型：</strong>{{ getServiceTypeText(afterSale.serviceType) }}</p>
                        <p><strong>申请原因：</strong>{{ afterSale.reason }}</p>
                        <p v-if="afterSale.description"><strong>问题描述：</strong>{{ afterSale.description }}</p>
                        <p v-if="afterSale.refundAmount"><strong>退款金额：</strong><span class="red--text">¥{{ afterSale.refundAmount }}</span></p>
                        <p><strong>申请时间：</strong>{{ formatTime(afterSale.createTime) }}</p>
                        <p v-if="afterSale.sellerReply"><strong>商家回复：</strong>{{ afterSale.sellerReply }}</p>
                      </v-col>
                      <v-col cols="12" md="4" v-if="afterSale.images && afterSale.images.length > 0">
                        <v-row>
                          <v-col cols="6" v-for="image in afterSale.images" :key="image.id">
                            <v-img :src="image.imageUrl" aspect-ratio="1" class="grey lighten-2"></v-img>
                          </v-col>
                        </v-row>
                      </v-col>
                    </v-row>
                  </v-card-text>

                  <v-card-actions>
                    <v-btn text color="primary" @click="viewDetail(afterSale.id)">查看详情</v-btn>
                  </v-card-actions>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
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
  name: 'AfterSaleList',
  components: {
    SnackbarNotification
  },
  data() {
    return {
      afterSales: []
    };
  },
  mounted() {
    this.loadAfterSales();
  },
  methods: {
    async loadAfterSales() {
      try {
        const response = await axios.get('/api/after-sales');
        this.afterSales = response.data;
      } catch (error) {
        this.$refs.snackbar.show('加载售后列表失败', 'error');
      }
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'PROCESSING': '处理中',
        'APPROVED': '已同意',
        'REJECTED': '已拒绝',
        'COMPLETED': '已完成'
      };
      return statusMap[status] || status;
    },
    getStatusColor(status) {
      const colorMap = {
        'PENDING': 'warning',
        'PROCESSING': 'info',
        'APPROVED': 'success',
        'REJECTED': 'error',
        'COMPLETED': 'grey'
      };
      return colorMap[status] || 'grey';
    },
    getServiceTypeText(type) {
      const typeMap = {
        'RETURN': '退货退款',
        'REFUND': '仅退款',
        'EXCHANGE': '换货'
      };
      return typeMap[type] || type;
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN');
    },
    viewDetail(id) {
      this.$router.push(`/after-sales/${id}`);
    }
  }
};
</script>
