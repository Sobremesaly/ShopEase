<template>
  <view class="register-container">
    <!-- 顶部导航栏 -->
    <uni-nav-bar
        title="用户注册"
        left-text="返回"
        @clickLeft="goBack"
        background-color="#fff"
        border-bottom="false"
    />

    <!-- 主体内容（卡片式表单） -->
    <view class="register-card">
      <uni-forms
          :model="form"
          :rules="formRules"
          ref="registerFormRef"
          label-width="100rpx"
      >
        <!-- 手机号 -->
        <uni-forms-item label="手机号" name="phone">
          <uni-easyinput
              v-model="form.phone"
              placeholder="请输入手机号"
              class="input-style"
              type="number"
              maxlength="11"
          />
        </uni-forms-item>

        <!-- 验证码 -->
        <uni-forms-item label="验证码" name="code">
          <view class="code-container">
            <uni-easyinput
                v-model="form.code"
                placeholder="请输入验证码"
                class="input-style code-input"
                type="number"
                maxlength="6"
            />
            <!-- 发送验证码按钮 -->
            <button
                class="send-code-btn"
                :disabled="isCounting"
                @click="sendCode"
            >
              {{ isCounting ? `${countDown}s后重发` : '发送验证码' }}
            </button>
          </view>
        </uni-forms-item>

        <!-- 密码 -->
        <uni-forms-item label="设置密码" name="password">
          <uni-easyinput
              v-model="form.password"
              type="password"
              :password-visibility="showPwd"
              placeholder="请设置密码（≥6位）"
              class="input-style"
              @click-icon="showPwd = !showPwd"
          />
        </uni-forms-item>

        <!-- 确认密码 -->
        <uni-forms-item label="确认密码" name="confirmPwd">
          <uni-easyinput
              v-model="form.confirmPwd"
              type="password"
              :password-visibility="showConfirmPwd"
              placeholder="请再次输入密码"
              class="input-style"
              @click-icon="showConfirmPwd = !showConfirmPwd"
          />
          <!-- 密码一致性提示 -->
          <view class="confirm-tip" v-if="form.confirmPwd.length > 0">
            <up-icon
                type="checkmark-circle"
                size="24rpx"
                color="#42b983"
                v-if="form.password === form.confirmPwd"
            />
            <up-icon
                type="close-circle"
                size="24rpx"
                color="#ff4d4f"
                v-else
            />
            <text class="tip-text" :style="{ color: form.password === form.confirmPwd ? '#42b983' : '#ff4d4f' }">
              {{ form.password === form.confirmPwd ? '密码一致' : '密码不一致' }}
            </text>
          </view>
        </uni-forms-item>

        <!-- 协议勾选 -->
        <uni-forms-item name="agreement" class="agreement-item">
          <view class="agreement-container">
            <checkbox
                v-model="form.agreement"
                class="agreement-checkbox"
            />
            <text class="agreement-text">
              我已阅读并同意
              <text class="protocol-text" @click="goProtocol">《用户服务协议》</text>
              和
              <text class="protocol-text" @click="goPrivacy">《隐私政策》</text>
            </text>
          </view>
        </uni-forms-item>
      </uni-forms>
    </view>

    <!-- 注册按钮（固定底部） -->
    <view class="register-btn-container">
      <button
          class="register-btn"
          :loading="isLoading"
          :disabled="isLoading || !form.agreement"
          @click="submitRegister"
      >
        注册并登录
      </button>

      <!-- 已有账号？去登录 -->
      <view class="login-link" @click="goLogin">
        <text class="link-text">已有账号？</text>
        <text class="login-text">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { post } from '@/utils/request';

// 状态管理
const registerFormRef = ref(null); // 表单引用
const isLoading = ref(false); // 加载状态
const isCounting = ref(false); // 验证码倒计时状态
const countDown = ref(60); // 倒计时秒数
let countdownTimer = null; // 倒计时定时器

// 密码显示切换
const showPwd = ref(false);
const showConfirmPwd = ref(false);

// 表单数据
const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPwd: '',
  agreement: false // 协议勾选状态
});

// 表单校验规则
const formRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' },
    { pattern: /^[\w]{6,20}$/, message: '密码仅支持字母、数字、下划线', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: checkSamePwd, message: '两次输入的密码不一致', trigger: 'blur' }
  ],
  agreement: [
    { required: true, message: '请阅读并同意用户协议和隐私政策', trigger: 'change' }
  ]
});

// 自定义校验：两次密码一致
const checkSamePwd = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

// 发送验证码
const sendCode = async () => {
  // 校验手机号
  if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
    uni.$u.toast('请输入正确的手机号');
    return;
  }

  try {
    isCounting.value = true;
    // 调用后端发送验证码接口（替换为你的真实接口）
    const res = await post('/sys/user/sendCode', {
      phone: form.phone.trim()
    });

    uni.$u.toast('验证码已发送，请注意查收');

    // 启动倒计时
    countdownTimer = setInterval(() => {
      countDown.value--;
      if (countDown.value <= 0) {
        clearInterval(countdownTimer);
        isCounting.value = false;
        countDown.value = 60;
      }
    }, 1000);
  } catch (error) {
    console.error('发送验证码失败：', error);
    uni.$u.toast(error.msg || '验证码发送失败，请重试');
    isCounting.value = false;
    countDown.value = 60;
  }
};

// 提交注册
const submitRegister = async () => {
  // 表单校验
  const valid = await registerFormRef.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;
    // 调用后端注册接口（替换为你的真实接口）
    const res = await post('/sys/user/register', {
      phone: form.phone.trim(),
      code: form.code.trim(),
      password: form.password.trim(),
      confirmPassword: form.confirmPwd.trim()
    });

    uni.$u.toast('注册成功，正在跳转登录页');
    // 注册成功后跳登录页（可带手机号回显）
    setTimeout(() => {
      uni.reLaunch({ url: `/pages/login/login?phone=${form.phone.trim()}` });
    }, 1500);
  } catch (error) {
    console.error('注册失败：', error);
    uni.$u.toast(error.msg || '注册失败，请重试');
  } finally {
    isLoading.value = false;
  }
};

// 跳转用户协议页（可选，后续可新增）
const goProtocol = () => {
  uni.navigateTo({ url: '/pages/agreement/protocol' });
};

// 跳转隐私政策页（可选，后续可新增）
const goPrivacy = () => {
  uni.navigateTo({ url: '/pages/agreement/privacy' });
};

// 返回上一页
const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

// 跳登录页
const goLogin = () => {
  uni.reLaunch({ url: `/pages/login/login?phone=${form.phone.trim()}` });
};

// 页面卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
</script>

<style scoped lang="scss">
.register-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 150rpx; // 给底部按钮留空间

  // 卡片容器
  .register-card {
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

    // 验证码容器
    .code-container {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .code-input {
        flex: 1;
      }

      .send-code-btn {
        width: 200rpx;
        height: 80rpx;
        background-color: #f5f5f5;
        color: #666;
        font-size: 26rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        &:disabled {
          background-color: #eee;
          color: #999;
        }
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

    // 协议勾选项
    .agreement-item {
      margin-top: 10rpx;

      .agreement-container {
        display: flex;
        align-items: center;
        gap: 12rpx;

        .agreement-checkbox {
          transform: scale(0.8);
        }

        .agreement-text {
          font-size: 24rpx;
          color: #666;
          line-height: 36rpx;
        }

        .protocol-text {
          color: #42b983;
          text-decoration: underline;
        }
      }
    }
  }

  // 注册按钮容器
  .register-btn-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    padding: 30rpx 20rpx;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20rpx;

    .register-btn {
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

    .login-link {
      display: flex;
      align-items: center;
      gap: 8rpx;

      .link-text {
        font-size: 26rpx;
        color: #666;
      }

      .login-text {
        font-size: 26rpx;
        color: #42b983;
        font-weight: 500;
      }
    }
  }
}
</style>
