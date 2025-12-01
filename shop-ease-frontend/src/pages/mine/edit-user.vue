<template>
  <view class="edit-user-container">
    <!-- 顶部导航栏 -->
    <uni-nav-bar
        title="编辑个人信息"
        left-text="返回"
        @clickLeft="goBack"
        background-color="#fff"
        border-bottom="false"
    />

    <!-- 主体内容（卡片式布局） -->
    <view class="edit-card">
      <!-- 头像上传区域 -->
      <view class="avatar-container">
        <view class="avatar-label">头像</view>
        <view class="avatar-upload">
          <!-- 头像预览 -->
          <image
              :src="avatarUrl"
              class="avatar-img"
              mode="aspectFill"
          />
          <!-- 上传按钮 -->
          <view class="upload-btn" @click="chooseAvatar">
            <up-icon type="camera" size="20" color="#fff" />
          </view>
        </view>
      </view>

      <!-- 表单区域 -->
      <uni-forms
          :model="form"
          :rules="formRules"
          ref="editFormRef"
          label-width="80rpx"
      >
        <!-- 昵称 -->
        <uni-forms-item label="昵称" name="nickname">
          <uni-easyinput
              v-model="form.nickname"
              placeholder="请输入昵称（2-10字）"
              class="input-style"
              @input="handleInput('nickname')"
          />
          <!-- 实时长度提示 -->
          <view class="length-tip" v-if="form.nickname.length > 0">
            {{ form.nickname.length }}/10
          </view>
        </uni-forms-item>

        <!-- 手机号（脱敏显示，不可编辑） -->
        <uni-forms-item label="手机号">
          <view class="phone-text">
            {{ form.phone ? form.phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3') : '未绑定' }}
          </view>
        </uni-forms-item>

        <!-- 性别选择 -->
        <uni-forms-item label="性别" name="gender">
          <view class="gender-group">
            <label class="gender-label" @click="form.gender = 1">
              <radio value="1" :checked="form.gender === 1" /> 男
            </label>
            <label class="gender-label" @click="form.gender = 2">
              <radio value="2" :checked="form.gender === 2" /> 女
            </label>
            <label class="gender-label" @click="form.gender = 0">
              <radio value="0" :checked="form.gender === 0" /> 保密
            </label>
          </view>
        </uni-forms-item>
      </uni-forms>
    </view>

    <!-- 保存按钮（固定底部） -->
    <view class="save-btn-container">
      <button
          class="save-btn"
          :loading="isLoading"
          :disabled="isLoading || !form.nickname.trim()"
          @click="submitForm"
      >
        保存修改
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useUserStore } from '@/stores/user';
import { post, upload } from '@/utils/request';

// 状态管理
const userStore = useUserStore();
const editFormRef = ref(null); // 表单引用
const isLoading = ref(false); // 加载状态
const avatarUrl = ref(''); // 头像URL

// 表单数据
const form = reactive({
  nickname: '',
  phone: '',
  gender: 0, // 0-保密，1-男，2-女
  avatar: '' // 头像URL（后端存储用）
});

// 表单校验规则
const formRules = reactive({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度为2-10字', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
});

// 页面挂载时回显用户信息
onMounted(() => {
  const userInfo = userStore.userInfo;
  form.nickname = userInfo.nickname || '';
  form.phone = userInfo.phone || '';
  form.gender = userInfo.gender || 0;
  // 头像回显（优先用用户信息中的头像，无则用默认图）
  avatarUrl.value = userInfo.avatar || '/static/default-avatar.png';
  form.avatar = userInfo.avatar || '';
});

// 输入框实时处理
const handleInput = (type) => {
  if (type === 'nickname') {
    // 限制昵称长度为10字
    if (form.nickname.length > 10) {
      form.nickname = form.nickname.slice(0, 10);
    }
  }
};

// 选择/更换头像
const chooseAvatar = async () => {
  try {
    // 选择本地图片
    const [res] = await uni.chooseImage({
      count: 1, // 仅选1张
      sizeType: ['original', 'compressed'], // 原图/压缩图
      sourceType: ['album', 'camera'], // 相册/相机
      crop: { // 裁剪配置（正方形头像）
        width: 200,
        height: 200,
        quality: 0.8
      }
    });

    // 上传头像到后端
    isLoading.value = true;
    const uploadRes = await upload('/sys/user/uploadAvatar', res.tempFilePaths[0], {
      loadingText: '上传中...',
      name: 'avatar' // 后端接收文件的字段名
    });

    // 上传成功，更新头像URL
    avatarUrl.value = uploadRes.data.url;
    form.avatar = uploadRes.data.url;
    uni.$u.toast('头像上传成功');
  } catch (error) {
    console.error('头像上传失败：', error);
    uni.$u.toast('头像上传失败，请重试');
  } finally {
    isLoading.value = false;
  }
};

// 提交表单
const submitForm = async () => {
  // 表单校验
  const valid = await editFormRef.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;
    // 调用后端更新接口（替换为你的真实接口）
    const res = await post('/sys/user/updateInfo', {
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar
    });

    // 提交成功，更新Pinia缓存
    userStore.setUserInfo({
      ...userStore,
      userInfo: {
        ...userStore.userInfo,
        nickname: form.nickname.trim(),
        gender: form.gender,
        avatar: form.avatar
      }
    });

    uni.$u.toast('修改成功');
    // 返回上一页并刷新数据
    uni.navigateBack({ delta: 1 });
  } catch (error) {
    console.error('修改失败：', error);
    uni.$u.toast(error.msg || '修改失败，请重试');
  } finally {
    isLoading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  uni.navigateBack({ delta: 1 });
};
</script>

<style scoped lang="scss">
.edit-user-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 120rpx; // 给底部按钮留空间

  // 卡片容器
  .edit-card {
    background-color: #fff;
    margin: 30rpx 20rpx;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    // 头像区域
    .avatar-container {
      display: flex;
      align-items: center;
      margin-bottom: 40rpx;

      .avatar-label {
        font-size: 32rpx;
        color: #333;
        margin-right: 30rpx;
        width: 80rpx; // 与表单label宽度一致
      }

      .avatar-upload {
        position: relative;

        .avatar-img {
          width: 160rpx;
          height: 160rpx;
          border-radius: 50%;
          border: 2rpx solid #eee;
        }

        .upload-btn {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 48rpx;
          height: 48rpx;
          background-color: #42b983;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 4rpx solid #fff;
        }
      }
    }

    // 输入框样式
    .input-style {
      font-size: 30rpx;
      color: #333;
    }

    // 长度提示
    .length-tip {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
      text-align: right;
    }

    // 手机号文本
    .phone-text {
      font-size: 30rpx;
      color: #333;
      line-height: 80rpx;
    }

    // 性别选择组
    .gender-group {
      display: flex;
      align-items: center;
      gap: 40rpx;
      padding: 20rpx 0;

      .gender-label {
        font-size: 30rpx;
        color: #333;
        display: flex;
        align-items: center;
        gap: 8rpx;

        radio {
          transform: scale(0.8);
        }
      }
    }
  }

  // 保存按钮容器
  .save-btn-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    padding: 20rpx;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

    .save-btn {
      width: 100%;
      height: 90rpx;
      background-color: #42b983;
      color: #fff;
      font-size: 32rpx;
      border-radius: 45rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &:disabled {
        background-color: #a3e6c6;
        color: #fff;
      }
    }
  }
}
</style>
