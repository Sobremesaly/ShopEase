<template>
  <view class="login-container">
    <!-- 顶部 Logo + 标题 -->
    <view class="login-header">
      <!-- 可替换为自己的 Logo（放在 static 目录下） -->
      <u-image
          src="/static/images/logo.png"
          mode="widthFix"
          class="logo"
      ></u-image>
      <view class="login-title">欢迎登录 ShopEase</view>
      <view class="login-desc">专注便捷购物体验</view>
    </view>

    <!-- 中间表单区域 -->
    <view class="login-form">
      <!-- 手机号/用户名输入框 -->
      <u-input
          v-model="form.account"
          placeholder="请输入手机号或用户名"
          prefix-icon="account"
          class="input-item"
          :border="borderType"
          @focus="handleInputFocus('account')"
          @blur="handleInputBlur('account')"
      />

      <!-- 密码输入框 -->
      <u-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="lock"
          class="input-item"
          :border="borderType"
          @focus="handleInputFocus('password')"
          @blur="handleInputBlur('password')"
          :show-password="showPwd"
          suffix-icon="eye"
          @click-icon="showPwd = !showPwd"
      />

      <!-- 记住密码 + 忘记密码 -->

      <view class="login-form-footer">
        <u-checkbox-group>
          <u-checkbox
              v-model="form.rememberPwd"
              shape="circle"
              active-color="#409eff"
              class="remember-pwd"
          >
            记住密码
          </u-checkbox>
        </u-checkbox-group>
        <u-link
            text="忘记密码？"
            class="forgot-link"
            @click="goForgotPwd"
        />
      </view>

      <!-- 登录按钮（渐变+加载状态） -->
      <u-button
          type="primary"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
          :disabled="loading"
          gradient
          color="#409eff"
          colorEnd="#66b1ff"
      >
        登录
      </u-button>
    </view>

    <!-- 底部其他登录方式 + 注册链接 -->
    <view class="login-footer">
      <view class="other-login">
        <view class="split-line"></view>
        <view class="other-login-text">其他登录方式</view>
        <view class="split-line"></view>
      </view>

      <!-- 第三方登录图标（微信/QQ） -->
      <view class="third-login">
        <u-icon
            name="weixin-fill"
            size="50rpx"
            color="#07C160"
            class="third-icon"
            @click="thirdLogin('weixin')"
        ></u-icon>
        <u-icon
            name="qq-fill"
            size="50rpx"
            color="#12B7F5"
            class="third-icon"
            @click="thirdLogin('qq')"
        ></u-icon>
      </view>

      <!-- 注册链接 -->
      <view class="register-container">
        还没账号？
        <u-link
            text="立即注册"
            class="register-link"
            @click="goRegister"
        />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user'; // 后续创建 Pinia 用户状态库
import { useRouter } from 'vue-router';
import { post } from '../../utils/request'; // 导入封装的POST请求
const router = useRouter();
const userStore = useUserStore(); // Pinia 存储用户信息

// 表单数据
const form = ref({
  account: '', // 账号（手机号/用户名）
  password: '', // 密码
  rememberPwd: false // 记住密码
});

// 状态控制
const loading = ref(false); // 登录加载状态
const showPwd = ref(false); // 显示密码
const borderType = ref('bottom'); // 输入框边框样式（聚焦时变化）
const activeInput = ref(''); // 当前聚焦的输入框

// 输入框聚焦/失焦动画
const handleInputFocus = (type) => {
  activeInput.value = type;
  borderType.value = 'surround'; // 聚焦时显示全边框
};
const handleInputBlur = (type) => {
  if (!form.value[type]) {
    borderType.value = 'bottom'; // 失焦且为空时恢复下划线
  }
};

// 页面挂载时，读取 Pinia 中记住的密码
onMounted(() => {
  if (userStore.rememberPwd && userStore.account) {
    form.value.account = userStore.account;
    form.value.password = userStore.password;
    form.value.rememberPwd = true;
  }
});

// 表单校验
const validateForm = () => {
  if (!form.value.account.trim()) {
    uni.$u.toast('请输入手机号或用户名');
    return false;
  }
  // 手机号格式校验（可选）
  const phoneReg = /^1[3-9]\d{9}$/;
  if (form.value.account.trim().length === 11 && !phoneReg.test(form.value.account.trim())) {
    uni.$u.toast('请输入正确的手机号');
    return false;
  }
  if (!form.value.password.trim()) {
    uni.$u.toast('请输入密码');
    return false;
  }
  if (form.value.password.trim().length < 6) {
    uni.$u.toast('密码长度不能少于6位');
    return false;
  }
  return true;
};

// 登录逻辑（后续替换为真实接口）
const handleLogin = async () => {
  if (!validateForm()) return;

  loading.value = true;
  try {
    // 1. 调用后端登录接口（替换为你的真实接口路径，参数和后端一致）
    const res = await post('/sys/user/login', {
      username: form.value.account.trim(),
      password: form.value.password.trim()
    });

    // 2. 登录成功：存储用户信息到Pinia（token+账号+用户信息）
    userStore.setUserInfo({
      account: form.value.account.trim(),
      password: form.value.rememberPwd ? form.value.password.trim() : '',
      rememberPwd: form.value.rememberPwd,
      token: res.data.token, // 存储后端返回的token（关键！后续接口需要）
      userInfo: res.data.userInfo // 存储用户头像、昵称等（可选）
    });

    // 3. 提示成功并跳转页面（跳转到个人中心或首页）
    uni.$u.toast(res.msg || '登录成功');
    router.push('/pages/mine/mine'); // 替换为你的目标页面路径

  } catch (error) {
    // 4. 登录失败：不跳转，提示错误（比如账号密码错误）
    console.error('登录失败：', error);
  } finally {
    loading.value = false; // 关闭加载状态
  }
};


// 跳转忘记密码页
const goForgotPwd = () => {
  uni.navigateTo({ url: '/pages/forgot-pwd/forgot-pwd' });
};

// 跳转注册页
const goRegister = () => {
  uni.navigateTo({ url: '/pages/register/register' });
};

// 第三方登录（微信/QQ）
const thirdLogin = (type) => {
  uni.$u.toast(`暂未开放${type === 'weixin' ? '微信' : 'QQ'}登录`);
  // 后续可接入微信开放平台/QQ互联接口
};
</script>

<style scoped lang="scss">
/* 页面容器：垂直居中 + 全屏背景 */
.login-container {
  min-height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 30rpx;
  box-sizing: border-box;
}

/* 顶部 Logo + 标题 */
.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.logo {
  width: 160rpx;
  height: auto;
  margin-bottom: 30rpx;
}

.login-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.login-desc {
  font-size: 24rpx;
  color: #999;
}

/* 表单区域 */
.login-form {
  width: 100%;
  background-color: #fff;
  padding: 60rpx 40rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

/* 输入框样式 */
.input-item {
  margin-bottom: 40rpx;
  --u-input-icon-color: #c0c4cc;
  --u-input-focus-icon-color: #409eff;
  --u-input-focus-border-color: #409eff;
  --u-input-font-size: 28rpx;
}

/* 记住密码 + 忘记密码 */
.login-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 50rpx;
}

.remember-pwd {
  font-size: 24rpx;
  color: #666;
  --u-checkbox-size: 24rpx;
}

.forgot-link {
  font-size: 24rpx;
  color: #409eff;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 90rpx;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 45rpx;
}

/* 底部其他登录方式 + 注册链接 */
.login-footer {
  width: 100%;
  text-align: center;
  margin-top: 60rpx;
}

/* 分割线 + 文字 */
.other-login {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.split-line {
  width: 30%;
  height: 1rpx;
  background-color: #eee;
}

.other-login-text {
  font-size: 24rpx;
  color: #999;
  padding: 0 20rpx;
}

/* 第三方登录图标 */
.third-login {
  display: flex;
  justify-content: center;
  gap: 60rpx;
  margin-bottom: 60rpx;
}

.third-icon {
  background-color: #fff;
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.third-icon:active {
  transform: scale(0.95);
}

/* 注册链接 */
.register-container {
  font-size: 26rpx;
  color: #666;
}

.register-link {
  color: #409eff;
  margin-left: 10rpx;
  font-weight: 500;
}
</style>