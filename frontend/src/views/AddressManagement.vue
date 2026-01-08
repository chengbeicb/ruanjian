<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title>
            <span class="text-h5">收货地址管理</span>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="openAddDialog">
              <v-icon left>mdi-plus</v-icon>
              添加新地址
            </v-btn>
          </v-card-title>

          <v-card-text>
            <v-row v-if="addresses.length === 0">
              <v-col cols="12" class="text-center py-8">
                <v-icon size="64" color="grey lighten-1">mdi-map-marker-off</v-icon>
                <p class="text-h6 mt-4 grey--text">暂无收货地址</p>
              </v-col>
            </v-row>

            <v-row v-else>
              <v-col cols="12" md="6" v-for="address in addresses" :key="address.id">
                <v-card outlined :class="{ 'primary lighten-5': address.isDefault }">
                  <v-card-title class="pb-2">
                    <span>{{ address.receiverName }}</span>
                    <v-spacer></v-spacer>
                    <v-chip small color="primary" v-if="address.isDefault">默认</v-chip>
                  </v-card-title>
                  <v-card-text>
                    <p><v-icon small>mdi-phone</v-icon> {{ address.receiverPhone }}</p>
                    <p><v-icon small>mdi-map-marker</v-icon> {{ address.province }} {{ address.city }} {{ address.district }}</p>
                    <p class="ml-6">{{ address.detailAddress }}</p>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn text color="primary" @click="editAddress(address)">编辑</v-btn>
                    <v-btn text color="success" @click="setDefault(address.id)" v-if="!address.isDefault">设为默认</v-btn>
                    <v-spacer></v-spacer>
                    <v-btn text color="error" @click="deleteAddress(address.id)">删除</v-btn>
                  </v-card-actions>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- 添加/编辑地址对话框 -->
    <v-dialog v-model="dialog" max-width="600px" persistent>
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ isEditMode ? '编辑地址' : '添加地址' }}</span>
        </v-card-title>
        <v-card-text>
          <v-form ref="form" v-model="valid">
            <v-text-field
              v-model="currentAddress.receiverName"
              label="收货人姓名"
              :rules="[rules.required]"
              required
            ></v-text-field>
            <v-text-field
              v-model="currentAddress.receiverPhone"
              label="联系电话"
              :rules="[rules.required, rules.phone]"
              required
            ></v-text-field>
            <v-row>
              <v-col cols="4">
                <v-text-field
                  v-model="currentAddress.province"
                  label="省份"
                  :rules="[rules.required]"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="4">
                <v-text-field
                  v-model="currentAddress.city"
                  label="城市"
                  :rules="[rules.required]"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="4">
                <v-text-field
                  v-model="currentAddress.district"
                  label="区县"
                  :rules="[rules.required]"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-textarea
              v-model="currentAddress.detailAddress"
              label="详细地址"
              :rules="[rules.required]"
              rows="3"
              required
            ></v-textarea>
            <v-checkbox
              v-model="currentAddress.isDefault"
              label="设为默认地址"
            ></v-checkbox>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="closeDialog">取消</v-btn>
          <v-btn color="primary" @click="saveAddress" :disabled="!valid">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <snackbar-notification ref="snackbar"></snackbar-notification>
  </v-container>
</template>

<script>
import axios from 'axios';
import SnackbarNotification from '@/components/SnackbarNotification.vue';

export default {
  name: 'AddressManagement',
  components: {
    SnackbarNotification
  },
  data() {
    return {
      addresses: [],
      dialog: false,
      valid: false,
      isEditMode: false,
      currentAddress: {
        id: null,
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: false
      },
      rules: {
        required: value => !!value || '必填项',
        phone: value => /^1[3-9]\d{9}$/.test(value) || '请输入正确的手机号'
      }
    };
  },
  mounted() {
    this.loadAddresses();
  },
  methods: {
    async loadAddresses() {
      try {
        const response = await axios.get('/api/addresses');
        this.addresses = response.data;
      } catch (error) {
        this.$refs.snackbar.show('加载地址失败', 'error');
      }
    },
    openAddDialog() {
      this.isEditMode = false;
      this.currentAddress = {
        id: null,
        receiverName: '',
        receiverPhone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: false
      };
      this.dialog = true;
    },
    editAddress(address) {
      this.isEditMode = true;
      this.currentAddress = { ...address };
      this.dialog = true;
    },
    closeDialog() {
      this.dialog = false;
      this.$refs.form.reset();
    },
    async saveAddress() {
      if (this.$refs.form.validate()) {
        try {
          if (this.isEditMode) {
            await axios.put(`/api/addresses/${this.currentAddress.id}`, this.currentAddress);
            this.$refs.snackbar.show('地址更新成功');
          } else {
            await axios.post('/api/addresses', this.currentAddress);
            this.$refs.snackbar.show('地址添加成功');
          }
          this.closeDialog();
          this.loadAddresses();
        } catch (error) {
          this.$refs.snackbar.show(error.response?.data?.message || '操作失败', 'error');
        }
      }
    },
    async setDefault(addressId) {
      try {
        await axios.put(`/api/addresses/${addressId}/default`);
        this.$refs.snackbar.show('默认地址设置成功');
        this.loadAddresses();
      } catch (error) {
        this.$refs.snackbar.show('设置失败', 'error');
      }
    },
    async deleteAddress(addressId) {
      if (confirm('确定要删除这个地址吗？')) {
        try {
          await axios.delete(`/api/addresses/${addressId}`);
          this.$refs.snackbar.show('地址删除成功');
          this.loadAddresses();
        } catch (error) {
          this.$refs.snackbar.show('删除失败', 'error');
        }
      }
    }
  }
};
</script>
