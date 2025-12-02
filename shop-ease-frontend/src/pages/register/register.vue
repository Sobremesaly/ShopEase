<template>
  <view class="register-container">
    <!-- 顶部导航栏：已修正为 u-navbar -->
    <up-navbar
        title="用户注册"
        left-text="返回"
        @click-left="goBack"
        :safe-area-inset-top="true"
        placeholder
    />

    <!-- 主体内容（卡片式表单） -->
    <view class="register-card">
      <!-- 使用 uView Plus 的 Form 组件 -->
      <up-form
          :model="form"
          :rules="formRules"
          ref="registerFormRef"
          label-width="150"
      >
        <!-- 手机号 -->
        <up-form-item label="手机号" prop="phone">
          <up-input
              v-model="form.phone"
              placeholder="请输入手机号"
              clearable
              maxlength="11"
              type="number"
          />
        </up-form-item>

        <!-- 验证码 -->
        <up-form-item label="验证码" prop="code">
          <view class="code-container">
            <up-input
                v-model="form.code"
                placeholder="请输入验证码"
                class="code-input"
                maxlength="6"
                type="number"
                clearable
            />
            <!-- 发送验证码按钮 -->
            <up-button
                class="send-code-btn"
                :disabled="isCounting || !isPhoneValid"
                @click="sendCode"
                size="mini"
                type="primary"
                plain
            >
              {{ isCounting ? `${countDown}s后重发` : '获取验证码' }}
            </up-button>
          </view>
        </up-form-item>

        <!-- 密码 -->
        <up-form-item label="设置密码" prop="password">
          <up-input
              v-model="form.password"
              placeholder="请设置密码（≥6位）"
              :password="!showPwd"
              clearable
          >
            <template #suffix>
              <u-icon
                  :name="showPwd ? 'eye-fill' : 'eye-off'"
                  size="20"
                  @click="showPwd = !showPwd"
              />
            </template>
          </up-input>
        </up-form-item>

        <!-- 确认密码 -->
        <up-form-item label="确认密码" prop="confirmPwd">
          <up-input
              v-model="form.confirmPwd"
              placeholder="请再次输入密码"
              :password="!showConfirmPwd"
              clearable
          >
            <template #suffix>
              <up-icon
                  :name="showConfirmPwd ? 'eye-fill' : 'eye-off'"
                  size="20"
                  @click="showConfirmPwd = !showConfirmPwd"
              />
            </template>
          </up-input>
          <!-- 密码一致性实时提示 -->
          <view class="confirm-tip" v-if="form.confirmPwd.length > 0">
            <up-icon
                :name="isPwdConsistent ? 'checkmark-circle-fill' : 'close-circle-fill'"
                size="16"
                :color="isPwdConsistent ? '#42b983' : '#fa3534'"
            />
            <text class="tip-text" :style="{ color: isPwdConsistent ? '#42b983' : '#fa3534' }">
              {{ isPwdConsistent ? '密码一致' : '密码不一致' }}
            </text>
          </view>
        </up-form-item>

        <!-- 协议勾选 -->
        <up-form-item prop="agreement">
          <view class="agreement-container">
            <up-checkbox-group>
              <up-checkbox
                  v-model="form.agreement"
                  icon-size="16"
                  shape="circle"
              >
                <text class="agreement-text">
                  我已阅读并同意
                  <text class="protocol-text" @click.stop="goProtocol">《用户服务协议》</text>
                  和
                  <text class="protocol-text" @click.stop="goPrivacy">《隐私政策》</text>
                </text>
              </up-checkbox>
            </up-checkbox-group>
          </view>
        </up-form-item>
      </up-form>
    </view>

    <!-- 注册按钮（固定底部） -->
    <view class="register-btn-container">
      <up-button
          class="register-btn"
          :loading="isLoading"
          :disabled="!form.agreement"
          type="primary"
          shape="circle"
          @click="submitRegister"
      >
        注册并登录
      </up-button>

      <!-- 已有账号？去登录 -->
      <view class="login-link">
        <text class="link-text">已有账号？</text>
        <text class="login-text" @click="goLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onUnmounted } from 'vue';
import { post } from '../../utils/request';

// 状态管理
const registerFormRef = ref(null);
const isLoading = ref(false);
const isCounting = ref(false);
const countDown = ref(60);
let countdownTimer = null;

// 密码显示切换
const showPwd = ref(false);
const showConfirmPwd = ref(false);

// 表单数据
const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPwd: '',
  agreement: false
});

// 计算属性：手机号格式是否有效、密码是否一致
const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(form.phone.trim()));
const isPwdConsistent = computed(() => form.password === form.confirmPwd);

// 表单校验规则
const formRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' },
    { pattern: /^[\w]{6,20}$/, message: '密码仅支持字母、数字、下划线', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  agreement: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议和隐私政策'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
});

// 发送验证码
const sendCode = async () => {
  if (!isPhoneValid.value) {
    uni.$u.toast('请输入正确的手机号');
    return;
  }

  try {
    isCounting.value = true;
    // TODO: 替换为你的真实接口路径
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
  try {
    // 进行表单校验
    const valid = await registerFormRef.value.validate();
    if (!valid) {
      uni.$u.toast('请检查表单信息');
      return;
    }

    isLoading.value = true;
    // TODO: 替换为你的真实注册接口路径和参数
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

// 跳转用户协议页
const goProtocol = () => {
  uni.navigateTo({ url: '/pages/agreement/protocol' });
};

// 跳转隐私政策页
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
  padding-bottom: 150rpx;

  :deep(.uni-input-placeholder) {
    font-size: 20rpx !important;
    color: #c0c4cc;
  }
  .register-card {
    background-color: #fff;
    margin: 30rpx 20rpx;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

    .code-container {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .code-input {
        flex: 1;
      }

      .send-code-btn {
        width: 200rpx;
        flex-shrink: 0;
      }
    }

    .confirm-tip {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-top: 10rpx;

      .tip-text {
        font-size: 24rpx;
      }
    }

    .agreement-container {
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
