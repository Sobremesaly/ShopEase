<template>
  <view class="edit-user-container">
    <!-- 顶部导航栏 -->
    <up-navbar
        title="编辑资料"
        left-text="返回"
        @leftClick="goBack"
        :safe-area-inset-top="true"
        background-color="#fff"
        border-bottom="false"
        title-color="#1a1a1a"
        left-text-color="#666"
        left-icon-color="#666"
        class="custom-navbar"
    />

    <!-- 主体内容 -->
    <view class="edit-card">
      <!-- 头像上传区域 -->
      <view class="section avatar-section">
        <view class="section-label">头像</view>
        <view class="avatar-content">
          <view class="avatar-wrapper" @click="chooseAvatar">
            <!-- 头像图片 -->
            <up-image
                :src="avatarUrl || '/static/avatar/icons10.png'"
                class="avatar-img"
                mode="aspectFill"
                width="180rpx"
                height="180rpx"
                shape="circle"
            />

            <!-- 半露式相机图标 -->
            <view class="camera-icon-btn" @click.stop="chooseAvatar">
              <up-icon name="camera" size="20" color="#fff"/>
            </view>
          </view>
        </view>
      </view>

      <!-- 表单区域 -->
      <view class="section form-section">
        <up-form
            :model="form"
            :rules="formRules"
            ref="editFormRef"
            class="edit-form"
        >
          <!-- 昵称 -->
          <view class="form-item">
            <view class="form-item-label">昵称</view>
            <view class="form-item-content">
              <up-input
                  v-model="form.nickname"
                  placeholder="请输入昵称"
                  placeholder-style="color: #ccc;"
                  border="none"
                  class="nickname-input"
                  @input="handleInput('nickname')"
                  style="padding: 0"
              />
              <view class="length-indicator">
                {{ form.nickname.length }}/10
              </view>
            </view>
          </view>

          <!-- 分隔线 -->
          <view class="divider"></view>

          <!-- 手机号 -->
          <view class="form-item">
            <view class="form-item-label">手机号</view>
            <view class="form-item-content">
              <text class="phone-text">
                {{ form.phone ? form.phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3') : '未绑定' }}
              </text>
            </view>
          </view>

          <!-- 分隔线 -->
          <view class="divider"></view>

          <!-- 性别 -->
          <view class="form-item">
            <view class="form-item-label">性别</view>
            <view class="form-item-content">
              <view class="gender-options">
                <view
                    class="gender-option"
                    :class="{ 'gender-option-active': form.gender === 1 }"
                    @click="form.gender = 1"
                >
                  <up-icon
                      name="man"
                      size="18"
                      :color="form.gender === 1 ? '#5A7DFF' : '#999'"
                  />
                  <text class="gender-text">男</text>
                </view>
                <view
                    class="gender-option"
                    :class="{ 'gender-option-active': form.gender === 2 }"
                    @click="form.gender = 2"
                >
                  <up-icon
                      name="woman"
                      size="18"
                      :color="form.gender === 2 ? '#5A7DFF' : '#999'"
                  />
                  <text class="gender-text">女</text>
                </view>
                <view
                    class="gender-option"
                    :class="{ 'gender-option-active': form.gender === 0 }"
                    @click="form.gender = 0"
                >
                  <up-icon
                      name="lock"
                      size="18"
                      :color="form.gender === 0 ? '#5A7DFF' : '#999'"
                  />
                  <text class="gender-text">保密</text>
                </view>
              </view>
            </view>
          </view>
        </up-form>
      </view>
    </view>

    <!-- 操作提示 -->
    <view class="hint-section">
      <up-icon name="info-circle" size="16" color="#FF9F0A" />
      <text class="hint-text">修改信息后需要重新登录生效</text>
    </view>

    <!-- 保存按钮 -->
    <view class="action-section">
      <up-button
          :loading="isLoading"
          :disabled="isLoading || !form.nickname.trim()"
          @click="submitForm"
          shape="circle"
          class="save-button"
      >
        <template v-if="!isLoading">
          <up-icon name="checkmark-circle" size="18" color="#fff" />
          <text class="button-text">保存修改</text>
        </template>
        <text v-else>保存中...</text>
      </up-button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useUserStore } from '../../stores/user';
import { put, upload } from '../../utils/request';

// 环境变量：后端网关地址
const BACKEND_BASE_URL = import.meta.env.VITE_GATEWAY_BASE_URL || 'http://localhost:8080';

// 状态管理：实例化用户Store
const userStore = useUserStore();

// 表单引用
const editFormRef = ref(null);

// 状态控制
const isLoading = ref(false);
const avatarUrl = ref(''); // 头像展示URL

// 表单数据
const form = reactive({
  nickname: '', // 昵称
  phone: '',    // 手机号（只读）
  gender: 0,    // 性别：0-保密，1-男，2-女
  avatar: ''    // 头像存储路径
});

// 表单校验规则（移除性别必填，因为默认值是0）
const formRules = reactive({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度为2-10字', trigger: 'blur' }
  ]
});

// 页面挂载时回显用户信息
onMounted(() => {
  // 获取Pinia中的用户信息
  const userInfo = userStore.userInfo || {};
  // 回显表单数据
  form.nickname = userInfo.nickname || '';
  form.phone = userInfo.phone || '';
  form.gender = userInfo.gender || 0;
  form.avatar = userInfo.avatar || '';

  // 处理头像URL：如果是相对路径则拼接网关地址，否则直接使用
  if (form.avatar) {
    avatarUrl.value = form.avatar.startsWith('http')
        ? form.avatar
        : `${BACKEND_BASE_URL}${form.avatar}`;
  } else {
    avatarUrl.value = '/static/avatar/icons10.png'; // 默认头像
  }
});

// 输入框实时处理：昵称长度限制
const handleInput = (type) => {
  if (type === 'nickname' && form.nickname.length > 10) {
    form.nickname = form.nickname.slice(0, 10);
  }
};

// 头像选择与上传方法
const chooseAvatar = async () => {
  try {
    // 1. 选择图片（uniapp原生API）
    const { tempFilePaths, errMsg } = await uni.chooseImage({
      count: 1, // 只能选1张
      sizeType: ['original', 'compressed'], // 原图/压缩图
      sourceType: ['album', 'camera'], // 相册/相机
      crop: { width: 400, height: 400, quality: 0.9 } // 裁剪为正方形
    });

    // 校验是否选择了图片
    if (!tempFilePaths || tempFilePaths.length === 0) {
      uni.showToast({ title: '未选择图片', icon: 'none', duration: 1500 });
      return;
    }

    // 2. 显示加载状态
    isLoading.value = true;
    uni.showLoading({ title: '上传中', mask: true });

    // 3. 调用文件上传接口（核心：只需要这一次请求，后端已完成文件上传+头像更新）
    // 关键：确保upload封装函数传递了Token请求头和正确的表单名
    const uploadResult = await upload(
        '/sys/user/uploadAvatar', // 后端接口路径
        tempFilePaths[0], // 临时文件路径
        {
          name: 'avatar' // 表单字段名，与后端@RequestParam("avatar")完全匹配
        },
        {
          // 传递Token请求头（后端JwtUtils需要从这里获取Token解析userId）
          headers: {
            'Authorization': `Bearer ${userStore.token}` // 你的Token字段
          }
        }
    );

    // 校验上传结果
    if (!uploadResult || !uploadResult.data) {
      throw new Error('头像上传失败，未获取到存储路径');
    }

    // 4. 更新本地头像信息（无需再调用PUT接口）
    form.avatar = uploadResult.data;
    avatarUrl.value = uploadResult.data.startsWith('http')
        ? uploadResult.data
        : `${BACKEND_BASE_URL}${uploadResult.data}`;

    // 5. 同步更新Pinia中的用户头像（可选，让个人中心等页面实时刷新）
    userStore.userInfo.avatar = avatarUrl.value;

    // 6. 提示成功
    uni.showToast({ title: '头像上传成功', icon: 'success', duration: 1500 });

  } catch (error) {
    // 错误处理：统一提示错误信息
    console.error('头像上传失败：', error);
    uni.showToast({
      title: error.message || '头像上传失败，请重试',
      icon: 'none',
      duration: 1500
    });
  } finally {
    // 关闭加载状态
    isLoading.value = false;
    uni.hideLoading();
  }
};

// 提交表单：更新用户信息
const submitForm = async () => {
  // 1. 表单校验
  const valid = await editFormRef.value.validate();
  if (!valid) return;

  try {
    // 2. 显示加载状态
    isLoading.value = true;

    // 3. 调用后端更新用户信息接口
    const updateRes = await put('/sys/user/current', {
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar
    });

    // 4. 更新Pinia中的用户信息（适配改造后的Store）
    userStore.userInfo = {
      ...userStore.userInfo,
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar
    };
    userStore.avatar = avatarUrl.value; // 更新Store中的头像

    // 5. 提示成功，并执行重新登录逻辑（匹配页面提示）
    uni.showToast({ title: '修改成功，请重新登录', icon: 'success', duration: 2000 });

    // 6. 延迟后退出登录并跳转登录页
    setTimeout(async () => {
      await userStore.logout(userStore.refreshToken); // 调用Store的退出登录方法
      uni.redirectTo({ url: '/pages/login/login' }); // 跳转到登录页
    }, 2000);

  } catch (error) {
    // 错误处理
    console.error('修改用户信息失败：', error);
    uni.showToast({
      title: error.msg || error.message || '修改失败，请重试',
      icon: 'none',
      duration: 1500
    });
  } finally {
    // 关闭加载状态
    isLoading.value = false;
  }
};

// 返回上一页（优先用navigateBack，兼容小程序/APP）
const goBack = () => {
  uni.navigateBack({ delta: 1 });
  // 如果是从tab页进来的，可改用：uni.switchTab({url:'/pages/mine/mine'});
};
</script>

<style scoped lang="scss">
.edit-user-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 140rpx;
  padding-top: calc(var(--status-bar-height) + 1rpx);
  margin: 0;
  box-sizing: border-box;
}

// 导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

// 主卡片
.edit-card {
  margin: 120rpx 24rpx 24rpx;
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
}

// 区块通用样式
.section {
  padding: 0 32rpx;

  .section-label {
    font-size: 32rpx;
    color: #1a1a1a;
    font-weight: 500;
    padding: 32rpx 0 24rpx;
  }
}

// 头像区块 - 修复部分
.avatar-section {
  padding: 40rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .section-label {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    padding: 0 0 24rpx; /* 添加底部间距 */
  }

  .avatar-content {
    display: flex;
    justify-content: center;
    margin-top: 20rpx; /* 添加与label的间距 */
  }

  .avatar-wrapper {
    position: relative;
    width: 180rpx; /* 修正：从18rpx改为180rpx */
    height: 180rpx; /* 修正：从18rpx改为180rpx */
    border-radius: 50%;
    cursor: pointer;

    /* 使用up-image自带的样式，去掉这里的border */
    .avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
    }

    // 半露式相机图标
    .camera-icon-btn {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 52rpx;
      height: 52rpx;
      background: linear-gradient(135deg, #5A7DFF 0%, #33C2FF 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4rpx solid #fff;
      box-shadow: 0 4rpx 12rpx rgba(90, 125, 255, 0.3);
      z-index: 100;
      transition: all 0.2s ease;

      &:active {
        transform: scale(0.92);
      }
    }
  }
}

// 表单区块
.form-section {
  .form-item {
    padding: 32rpx 0;

    .form-item-label {
      font-size: 30rpx;
      color: #1a1a1a;
      margin-bottom: 20rpx;
      font-weight: 500;
    }

    .form-item-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .nickname-input {
      flex: 1;
      font-size: 30rpx;
      color: #1a1a1a;
      height: 80rpx;
      padding: 0;

      :deep(.u-input__inner) {
        height: 100%;
      }
    }

    .length-indicator {
      font-size: 26rpx;
      color: #999;
      margin-left: 24rpx;
      min-width: 80rpx;
      text-align: right;
    }

    .phone-text {
      font-size: 30rpx;
      color: #1a1a1a;
    }
  }

  .gender-options {
    display: flex;
    gap: 40rpx;

    .gender-option {
      display: flex;
      align-items: center;
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      background: #f8f9fa;
      border: 2rpx solid #f8f9fa;
      transition: all 0.2s ease;
      cursor: pointer;

      .gender-text {
        font-size: 28rpx;
        color: #666;
        margin-left: 12rpx;
        transition: color 0.2s ease;
      }

      &-active {
        background: rgba(90, 125, 255, 0.1);
        border-color: #5A7DFF;

        .gender-text {
          color: #5A7DFF;
          font-weight: 500;
        }
      }

      &:active {
        transform: scale(0.98);
      }
    }
  }
}

// 分隔线
.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 0 -32rpx;
}

// 提示区块
.hint-section {
  margin: 32rpx 24rpx 0;
  padding: 24rpx 32rpx;
  background: rgba(255, 159, 10, 0.08);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;

  .hint-text {
    font-size: 26rpx;
    color: #FF9F0A;
    flex: 1;
  }
}

// 操作区块
.action-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 24rpx 32rpx 40rpx;
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.05);

  .save-button {
    height: 96rpx;
    background: linear-gradient(135deg, #5A7DFF, #33C2FF);
    border: none;

    .button-text {
      margin-left: 12rpx;
      font-size: 32rpx;
      font-weight: 500;
    }

    &:disabled {
      opacity: 0.6;
    }

    &:active:not(:disabled) {
      transform: scale(0.98);
    }
  }
}
</style>