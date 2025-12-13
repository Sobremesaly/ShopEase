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
                :src="avatarUrl == null ? '/static/avatar/icons10.png' : avatarUrl"
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
import {ref, onMounted, reactive} from 'vue';
import {useUserStore} from '../../stores/user';
import {post, put, upload} from '../../utils/request';
import uploadRes from "uview-ui/libs/mixin/mixin";
const BACKEND_BASE_URL = import.meta.env.VITE_GATEWAY_BASE_URL || 'http://localhost:8080';
// 状态管理
const userStore = useUserStore();
const editFormRef = ref(null);
const isLoading = ref(false);
const avatarUrl = ref('');

// 表单数据
const form = reactive({
  nickname: '',
  phone: '',
  gender: 0,
  avatar: ''
});

// 表单校验规则
const formRules = reactive({
  nickname: [
    {required: true, message: '请输入昵称', trigger: 'blur'},
    {min: 2, max: 10, message: '昵称长度为2-10字', trigger: 'blur'}
  ],
  gender: [
    {required: true, message: '请选择性别', trigger: 'change'}
  ]
});

// 页面挂载时回显用户信息
let userInfo = '';
onMounted(() => {
  userInfo = userStore.userInfo;
  form.nickname = userInfo.nickname || '';
  form.phone = userInfo.phone || '';
  form.gender = userInfo.gender || 0;
  avatarUrl.value = userInfo.avatar
      ? `${BACKEND_BASE_URL}${userInfo.avatar}`
      : '/static/default-avatar.png';
  form.avatar = userInfo.avatar || '';
});

// 输入框实时处理
const handleInput = (type) => {
  if (type === 'nickname') {
    if (form.nickname.length > 10) {
      form.nickname = form.nickname.slice(0, 10);
    }
  }
};

// 选择/更换头像
const chooseAvatar = async () => {
  try {
    const {tempFilePaths} = await uni.chooseImage({
      count: 1,
      sizeType: ['original', 'compressed'],
      sourceType: ['album', 'camera'],
      crop: {width: 400, height: 400, quality: 0.9}
    });

    if (!tempFilePaths || tempFilePaths.length === 0) {
      await uni.showToast({title: '未选择图片', icon: 'none', duration: 1500});
      return;
    }

    isLoading.value = true;
    await uni.showLoading({title: '上传中', mask: true});

    const uploadRes = await upload('/sys/user/uploadAvatar', tempFilePaths[0], {
      name: 'avatar'
    });

    avatarUrl.value = uploadRes.data;
    form.avatar = uploadRes.data;
    await uni.showToast({title: uploadRes.data.msg, icon: 'success', duration: 1500});
  } catch (error) {
    console.error('头像上传失败：', error);
    await uni.showToast({title: uploadRes.data.msg, icon: 'none', duration: 1500});
  } finally {
    isLoading.value = false;
    uni.hideLoading();
  }
};

// 提交表单
const submitForm = async () => {
  const valid = await editFormRef.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;
    const res = await put('/sys/user/current', {
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar
    });

    // 更新Pinia缓存
    userStore.setUserInfo({
      ...userStore,
      userInfo: {
        ...userStore.userInfo,
        nickname: form.nickname.trim(),
        gender: form.gender,
        avatar: form.avatar
      }
    });

    await uni.showToast({
      title: '修改成功',
      icon: 'success',
      duration: 1500
    });

    /*setTimeout(() => {
      uni.navigateBack({delta: 1});
    }, 1500);*/

  } catch (error) {
    console.error('修改失败：', error);
    await uni.showToast({
      title: error.msg || '修改失败，请重试',
      icon: 'none',
      duration: 1500
    });
  } finally {
    isLoading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  uni.switchTab({url:'/pages/mine/mine'});
};
</script>

<style scoped lang="scss">
.edit-user-container {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 140rpx;
  /* 【重要】 只保留此处作为顶部的内边距，并确保值正确 */
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

// 头像区块
.avatar-section {
  .avatar-content {
    display: flex;
    justify-content: center;
    padding: 0;
  }

  .avatar-wrapper {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;

    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: all 0.3s ease;
    }

    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s ease;

      .overlay-text {
        color: #fff;
        font-size: 24rpx;
        margin-top: 8rpx;
      }
    }

    &:hover .avatar-overlay {
      opacity: 1;
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

// 头像区域样式优化
.avatar-section {
  padding: 40rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .section-label {
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    padding: 0;
  }

  .avatar-content {
    display: flex;
    justify-content: center;

    .avatar-wrapper {
      position: relative;
      width: 180rpx;
      height: 180rpx;
      border-radius: 50%;
      cursor: pointer;
      overflow: visible;

      .avatar-img {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
      }

      // 方案一：半露式相机图标
      .camera-icon-btn {
        position: absolute;
        bottom: -10rpx;    // 调整这个值控制露出多少
        right: -10rpx;     // 调整这个值控制露出多少
        width: 52rpx;     // 缩小尺寸
        height: 52rpx;
        background: linear-gradient(135deg, #5A7DFF 0%, #33C2FF 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 3rpx solid #fff;
        box-shadow: 0 4rpx 12rpx rgba(90, 125, 255, 0.3);
        z-index: 100;
        transition: all 0.2s ease;

        &:active {
          transform: scale(0.92);
        }
      }
    }
  }
}
</style>
