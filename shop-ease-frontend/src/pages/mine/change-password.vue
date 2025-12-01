<template>
  <view class="change-pwd-container">
    <!-- 顶部导航栏 -->
    <uni-nav-bar
        title="修改密码"
        left-text="返回"
        @clickLeft="goBack"
        background-color="#fff"
        border-bottom="false"
    />

    <!-- 主体内容（卡片式布局） -->
    <view class="change-card">
      <uni-forms
          :model="form"
          :rules="formRules"
          ref="pwdFormRef"
          label-width="100rpx"
      >
        <!-- 原密码 -->
        <uni-forms-item label="原密码" name="oldPwd">
          <uni-easyinput
              v-model="form.oldPwd"
              type="password"
              :password-visibility="showOldPwd"
              placeholder="请输入原密码"
              class="input-style"
              @click-icon="showOldPwd = !showOldPwd"
          />
        </uni-forms-item>

        <!-- 新密码 -->
        <uni-forms-item label="新密码" name="newPwd">
          <uni-easyinput
              v-model="form.newPwd"
              type="password"
              :password-visibility="showNewPwd"
              placeholder="请输入新密码（≥6位）"
              class="input-style"
              @input="checkPwdStrength"
              @click-icon="showNewPwd = !showNewPwd"
          />
          <!-- 密码强度提示 -->
          <view class="pwd-strength" v-if="form.newPwd.length > 0">
            <text class="strength-label">密码强度：</text>
            <view class="strength-bars">
              <view
                  class="strength-bar"
                  :class="{
                  weak: pwdStrength === 1,
                  medium: pwdStrength === 2,
                  strong: pwdStrength === 3
                }"
              ></view>
              <view
                  class="strength-bar"
                  :class="{
                  medium: pwdStrength === 2,
                  strong: pwdStrength === 3
                }"
              ></view>
              <view
                  class="strength-bar"
                  :class="{ strong: pwdStrength === 3 }"
              ></view>
            </view>
            <text class="strength-text">{{ strengthText }}</text>
          </view>
        </uni-forms-item>

        <!-- 确认新密码 -->
        <uni-forms-item label="确认密码" name="confirmPwd">
          <uni-easyinput
              v-model="form.confirmPwd"
              type="password"
              :password-visibility="showConfirmPwd"
              placeholder="请再次输入新密码"
              class="input-style"
              @click-icon="showConfirmPwd = !showConfirmPwd"
          />
          <!-- 密码一致性提示 -->
          <view class="confirm-tip" v-if="form.confirmPwd.length > 0">
            <up-icon
                type="checkmark-circle"
                size="24rpx"
                color="#42b983"
                v-if="form.newPwd === form.confirmPwd"
            />
            <up-icon
                type="close-circle"
                size="24rpx"
                color="#ff4d4f"
                v-else
            />
            <text class="tip-text" :style="{ color: form.newPwd === form.confirmPwd ? '#42b983' : '#ff4d4f' }">
              {{ form.newPwd === form.confirmPwd ? '密码一致' : '密码不一致' }}
            </text>
          </view>
        </uni-forms-item>
      </uni-forms>

      <!-- 安全提示 -->
      <view class="safe-tip">
        <up-icon type="info-circle" size="24rpx" color="#999" />
        <text class="tip-content">请设置与原密码不同的新密码，建议包含字母和数字</text>
      </view>
    </view>

    <!-- 保存按钮（固定底部） -->
    <view class="save-btn-container">
      <button
          class="save-btn"
          :loading="isLoading"
          :disabled="isLoading || !isFormValid"
          @click="submitForm"
      >
        确认修改
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { post } from '@/utils/request';

// 状态管理
const userStore = useUserStore();
const pwdFormRef = ref(null); // 表单引用
const isLoading = ref(false); // 加载状态

// 密码显示切换
const showOldPwd = ref(false);
const showNewPwd = ref(false);
const showConfirmPwd = ref(false);

// 表单数据
const form = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
});

// 密码强度相关
const pwdStrength = ref(0); // 0-无，1-弱，2-中，3-强
const strengthText = ref(''); // 强度文本提示

// 表单校验规则
const formRules = reactive({
  oldPwd: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '原密码长度不少于6位', trigger: 'blur' }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不少于6位', trigger: 'blur' },
    { validator: checkDiffPwd, message: '新密码与原密码不能相同', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: checkSamePwd, message: '两次输入的密码不一致', trigger: 'blur' }
  ]
});

// 自定义校验：新密码与原密码不同
const checkDiffPwd = (rule, value, callback) => {
  if (value === form.oldPwd) {
    callback(new Error('新密码与原密码不能相同'));
  } else {
    callback();
  }
};

// 自定义校验：两次密码一致
const checkSamePwd = (rule, value, callback) => {
  if (value !== form.newPwd) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

// 表单是否有效（用于按钮禁用）
const isFormValid = computed(() => {
  return form.oldPwd.trim() && form.newPwd.trim() && form.confirmPwd.trim() && form.newPwd === form.confirmPwd;
});

// 检查密码强度
const checkPwdStrength = (value) => {
  const pwd = value.trim();
  if (pwd.length < 6) {
    pwdStrength.value = 0;
    strengthText.value = '';
    return;
  }

  // 简单强度算法：纯数字/字母（弱）、混合（中）、含特殊字符（强）
  const hasNum = /\d/.test(pwd);
  const hasLetter = /[a-zA-Z]/.test(pwd);
  const hasSpecial = /[^a-zA-Z0-9]/.test(pwd);

  if ((hasNum && !hasLetter && !hasSpecial) || (hasLetter && !hasNum && !hasSpecial)) {
    pwdStrength.value = 1;
    strengthText.value = '弱';
  } else if ((hasNum && hasLetter) || (hasNum && hasSpecial) || (hasLetter && hasSpecial)) {
    pwdStrength.value = 2;
    strengthText.value = '中';
  } else if (hasNum && hasLetter && hasSpecial) {
    pwdStrength.value = 3;
    strengthText.value = '强';
  }
};

// 提交表单
const submitForm = async () => {
  // 表单校验
  const valid = await pwdFormRef.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;
    // 调用后端修改密码接口（替换为你的真实接口）
    const res = await post('/sys/user/changePwd', {
      oldPassword: form.oldPwd.trim(),
      newPassword: form.newPwd.trim(),
      confirmPassword: form.confirmPwd.trim()
    });

    uni.$u.toast('密码修改成功，请重新登录');
    // 退出登录，跳转登录页
    userStore.logout();
    uni.reLaunch({ url: '/pages/login/login' });
  } catch (error) {
    console.error('修改失败：', error);
    uni.$u.toast(error.msg || '修改失败，请检查原密码是否正确');
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
.change-pwd-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 120rpx; // 给底部按钮留空间

  // 卡片容器
  .change-card {
    background-color: #fff;
    margin: 30rpx 20rpx;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    // 输入框样式
    .input-style {
      font-size: 30rpx;
      color: #333;
    }

    // 密码强度提示
    .pwd-strength {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-top: 10rpx;

      .strength-label {
        font-size: 24rpx;
        color: #999;
      }

      .strength-bars {
        display: flex;
        gap: 6rpx;
        flex: 1;

        .strength-bar {
          height: 8rpx;
          flex: 1;
          background-color: #eee;
          border-radius: 4rpx;
        }

        .weak { background-color: #ff4d4f; }
        .medium { background-color: #faad14; }
        .strong { background-color: #42b983; }
      }

      .strength-text {
        font-size: 24rpx;
        &.weak { color: #ff4d4f; }
        &.medium { color: #faad14; }
        &.strong { color: #42b983; }
      }
    }

    // 确认密码提示
    .confirm-tip {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 10rpx;

      .tip-text {
        font-size: 24rpx;
      }
    }

    // 安全提示
    .safe-tip {
      display: flex;
      align-items: flex-start;
      gap: 10rpx;
      margin-top: 30rpx;
      padding: 20rpx;
      background-color: #f0f9fb;
      border-radius: 12rpx;

      .tip-content {
        font-size: 24rpx;
        color: #666;
        line-height: 36rpx;
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
