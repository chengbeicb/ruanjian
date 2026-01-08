<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card>
          <v-card-title>
            <v-btn icon @click="$router.go(-1)">
              <v-icon>mdi-arrow-left</v-icon>
            </v-btn>
            物流跟踪
          </v-card-title>

          <v-card-text v-if="logistics">
            <v-row>
              <v-col cols="12">
                <v-alert type="info" outlined>
                  <strong>物流公司：</strong>{{ logistics.logisticsCompany }}<br>
                  <strong>物流单号：</strong>{{ logistics.logisticsNumber }}<br>
                  <strong>当前状态：</strong>{{ logistics.currentStatus }}
                </v-alert>
              </v-col>

              <v-col cols="12">
                <h3 class="mb-4">物流轨迹</h3>
                <v-timeline dense>
                  <v-timeline-item
                    v-for="(track, index) in tracks"
                    :key="track.id"
                    :color="index === 0 ? 'primary' : 'grey'"
                    small
                  >
                    <template v-slot:opposite>
                      <span class="text-caption">{{ formatTime(track.trackTime) }}</span>
                    </template>
                    <v-card flat>
                      <v-card-subtitle class="pb-0">
                        <v-icon small :color="index === 0 ? 'primary' : 'grey'">
                          {{ getStatusIcon(track.trackStatus) }}
                        </v-icon>
                        {{ track.trackStatus }}
                        <span v-if="track.location" class="ml-2 grey--text">{{ track.location }}</span>
                      </v-card-subtitle>
                      <v-card-text>{{ track.trackInfo }}</v-card-text>
                    </v-card>
                  </v-timeline-item>
                </v-timeline>
              </v-col>
            </v-row>
          </v-card-text>

          <v-card-text v-else>
            <v-alert type="warning" outlined>
              暂无物流信息
            </v-alert>
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
  name: 'LogisticsTracking',
  components: {
    SnackbarNotification
  },
  data() {
    return {
      logistics: null,
      tracks: []
    };
  },
  mounted() {
    this.loadLogistics();
  },
  methods: {
    async loadLogistics() {
      const orderId = this.$route.params.orderId;
      try {
        const response = await axios.get(`/api/logistics/order/${orderId}`);
        if (response.data.message) {
          this.$refs.snackbar.show(response.data.message, 'warning');
          return;
        }
        this.logistics = response.data;
        
        // 加载物流轨迹
        const tracksResponse = await axios.get(`/api/logistics/${this.logistics.id}/tracks`);
        this.tracks = tracksResponse.data;
      } catch (error) {
        this.$refs.snackbar.show('加载物流信息失败', 'error');
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    getStatusIcon(status) {
      const iconMap = {
        '已发货': 'mdi-package-variant',
        '运输中': 'mdi-truck-delivery',
        '派送中': 'mdi-bike',
        '已签收': 'mdi-check-circle'
      };
      return iconMap[status] || 'mdi-map-marker';
    }
  }
};
</script>
