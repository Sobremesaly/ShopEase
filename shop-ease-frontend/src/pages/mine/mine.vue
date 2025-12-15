<template>
  <view class="mine-container">
    <!-- 顶部背景图 -->
    <view class="top-bg">
      <up-image src="/static/images/img.png" mode="aspectFill" width="100%" class="bg-img" />
    </view>

    <!-- 用户信息卡片（悬浮在背景图上） -->
    <view class="user-card">
      <!-- 头像 + 昵称 + 手机号 -->
      <view class="user-info">
        <up-image
            :src="avatarUrl || '/static/avatar/icons10.png'"
            class="avatar"
            mode="aspectFill"
            shape="circle"
            width="180rpx"
            height="180rpx"
        />
        <view class="info-right">
          <view class="nickname">{{ userStore.userInfo.nickname || '未设置昵称' }}</view>
          <view class="phone">
            {{ userStore.userInfo.phone ? userStore.userInfo.phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3') : '未绑定手机' }}
          </view>
        </view>
        <!-- 编辑按钮 -->
        <view class="edit-btn" @click="goEditUser">
          <up-icon name="/static/mine/icons2.png" size="22rpx" color="#fff" />
          <text class="edit-text">编辑</text>
        </view>
      </view>

      <!-- 功能标签（可选，如会员、积分） -->
      <view class="user-tags" v-if="userStore.userInfo.memberLevel">
        <view class="tag member-tag">
          <up-icon type="vip" size="20rpx" color="#ff9f43" />
          <text class="tag-text">会员等级 {{ userStore.userInfo.memberLevel }}</text>
        </view>
        <view class="tag point-tag">
          <up-icon type="coins" size="20rpx" color="#ffd166" />
          <text class="tag-text">积分 {{ userStore.userInfo.points || 0 }}</text>
        </view>
      </view>

      <!-- ================ 修改：订单状态选项栏 ================ -->
      <view class="order-tabs" v-if="userStore.isLogin">
        <view class="order-tab-item" v-for="item in orderTabList" :key="item.type" @click="goOrderList(item.type)">
          <view class="icon-wrapper">
            <up-image
                :src="item.icon"
                class="tab-icon"
                width="44rpx"
                height="44rpx"
                mode="aspectFit"
            />
            <!-- 角标相对于图标定位 -->
            <view class="badge" v-if="item.count > 0">{{ item.count }}</view>
          </view>
          <text class="tab-text">{{ item.name }}</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="func-list">
      <!-- 第一组功能 -->
      <view class="func-group">
        <view class="func-item" @click="goChangePwd">
          <up-icon name="/static/mine/icons1.png" size="32rpx" color="#42b983" class="func-icon" />
          <text class="func-text">修改密码</text>
          <up-icon name="/static/mine/icons5.png" size="48rpx" color="#ccc" class="right-icon" />
        </view>
        <view class="func-item" @click="goBindPhone" v-if="!userStore.userInfo.phone">
          <up-icon name="/static/mine/icons7.png" size="32rpx" color="#42b983" class="func-icon" />
          <text class="func-text">绑定手机号</text>
          <up-icon name="/static/mine/icons5.png" size="48rpx" color="#ccc" class="right-icon" />
        </view>
      </view>

      <!-- 第二组功能 -->
      <view class="func-group">
        <view class="func-item" @click="goFeedback">
          <up-icon name="/static/mine/icons3.png" size="32rpx" color="#42b983" class="func-icon" />
          <text class="func-text">意见反馈</text>
          <up-icon name="/static/mine/icons5.png" size="48rpx" color="#ccc" class="right-icon" />
        </view>
        <view class="func-item" @click="goAbout">
          <up-icon name="/static/mine/icons4.png" size="32rpx" color="#42b983" class="func-icon" />
          <text class="func-text">关于我们</text>
          <up-icon name="/static/mine/icons5.png" size="48rpx" color="#ccc" class="right-icon" />
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-item" @click="handleLogout">
        <up-icon name="/static/mine/icons6.png" size="32rpx" color="#ff4d4f" class="func-icon" />
        <text class="func-text" style="color: #ff4d4f;">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useUserStore } from '../../stores/user';
import { get } from '../../utils/request';

// 环境变量：后端网关地址
const BACKEND_BASE_URL = import.meta.env.VITE_GATEWAY_BASE_URL || 'http://localhost:8080';
// 状态管理：实例化用户Store（直接使用，无需ref包裹）
const userStore = useUserStore();
// 状态控制
const isLoading = ref(false);
const avatarUrl = ref('');

// ================ 修改：订单选项栏相关数据 ================
// 订单标签列表（可根据后端返回的统计数动态更新）
const orderTabList = ref([
  { type: 'all', name: '全部订单', icon: '/static/mine/order/all_order.png', count: 0 },
  { type: 'pending_pay', name: '待支付', icon: '/static/mine/order/pending_payment.png', count: 2 },
  { type: 'pending_receive', name: '待收货', icon: '/static/mine/order/received.png', count: 1 },
  { type: 'pending_evaluate', name: '待评价', icon: '/static/mine/order/evaluated.png', count: 3 },
  { type: 'after_sale', name: '售后', icon: '/static/mine/order/after_sales_service.png', count: 0 },
]);

// 辅助方法：更新头像URL（移到watch之前，解决初始化顺序问题）
const updateAvatarUrl = (avatarPath) => {
  if (avatarPath) {
    // 判断是否为绝对路径，是则直接使用，否则拼接网关地址
    avatarUrl.value = avatarPath.startsWith('http')
        ? avatarPath
        : `${BACKEND_BASE_URL}${avatarPath}`;
  } else {
    avatarUrl.value = '/static/avatar/icons10.png'; // 默认头像
  }
};

// 监听userStore的userInfo变化，自动更新头像URL（响应式）
watch(
    () => userStore.userInfo,
    (newUserInfo) => {
      updateAvatarUrl(newUserInfo.avatar); // 此时函数已存在，不会报错
    },
    { immediate: true } // 立即执行也没问题
);

// 页面挂载时的逻辑：校验登录态 + 获取最新用户信息 + 获取订单统计
onMounted(async () => {
  // 前置校验：未登录则跳转到登录页
  if (!userStore.isLogin) {
    uni.reLaunch({ url: '/pages/login/login' });
    return;
  }

  // 已登录，获取最新用户信息（避免缓存过期）
  try {
    isLoading.value = true;
    const res = await get('/sys/user/current'); // 调用后端获取最新信息
    // 更新Pinia的用户信息（直接赋值，保持响应式）
    userStore.userInfo = res.data;

    // ================ 新增：获取订单统计数据（替换模拟数据） ================
    // const orderStatRes = await get('/order/stat'); // 后端订单统计接口
    // orderTabList.value = orderStatRes.data; // 用后端数据替换本地模拟数据
  } catch (error) {
    console.error('获取用户信息失败：', error);
    // 失败时提示用户，仍使用缓存信息
    uni.$u.toast(error.msg || '获取用户信息失败，将显示本地缓存');
  } finally {
    isLoading.value = false;
  }
});

// 跳转编辑个人信息页
const goEditUser = () => {
  uni.navigateTo({ url: '/pages/mine/edit-user' });
};

// ================ 新增：跳转订单列表页（携带订单类型参数） ================
const goOrderList = (orderType) => {
  uni.navigateTo({
    url: `/pages/order/list?type=${orderType}`, // 订单列表页路径，需根据实际项目调整
  });
};

// 跳转修改密码页
const goChangePwd = () => {
  uni.navigateTo({ url: '/pages/mine/change-pwd' });
};

// 跳转绑定手机号页（可选，后续可新增）
const goBindPhone = () => {
  uni.navigateTo({ url: '/pages/mine/bind-phone' });
};

// 跳转意见反馈页（可选，后续可新增）
const goFeedback = () => {
  uni.navigateTo({ url: '/pages/mine/feedback' });
};

// 跳转关于我们页（可选，后续可新增）
const goAbout = () => {
  uni.navigateTo({ url: '/pages/mine/about' });
};

// 退出登录：调用Store的logout方法（内部会清理后端Refresh Token + 本地状态）
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#ff4d4f',
    cancelColor: '#666',
    async success (res) {
      if (res.confirm) {
        // 调用Store的退出登录方法（内部已处理后端接口和本地缓存）
        await userStore.logout();
        // 跳转到登录页
        uni.reLaunch({ url: '/pages/login/login' });
        // 提示退出成功
        uni.$u.toast('退出登录成功');
      }
    }
  });
};
</script>

<style scoped lang="scss">
.mine-container {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 30rpx;

  // 顶部背景图
  .top-bg {
    width: 100%;
    height: 240rpx;
    overflow: hidden;

    .bg-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  // 用户信息卡片
  .user-card {
    background-color: #fff;
    margin: -80rpx 30rpx 30rpx;
    border-radius: 20rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    position: relative;

    // 用户信息（头像+昵称+手机号+编辑）
    .user-info {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .avatar {
        width: 140rpx; // 由样式控制尺寸，移除标签上的width/height
        height: 140rpx;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      }

      .info-right {
        margin-left: 20rpx;
        flex: 1;

        .nickname {
          font-size: 34rpx;
          color: #333;
          font-weight: 500;
          margin-bottom: 8rpx;
        }

        .phone {
          font-size: 26rpx;
          color: #666;
        }
      }

      .edit-btn {
        background-color: #42b983;
        color: #fff;
        padding: 8rpx 20rpx;
        border-radius: 30rpx;
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 24rpx;
        transition: all 0.2s ease;

        &:active {
          transform: scale(0.95);
        }
      }
    }

    // 功能标签
    .user-tags {
      display: flex;
      gap: 16rpx;
      flex-wrap: wrap;
      margin-bottom: 20rpx; // 新增：与订单栏拉开间距

      .tag {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 6rpx 16rpx;
        border-radius: 20rpx;
        font-size: 22rpx;

        .tag-text {
          color: #666;
        }
      }

      .member-tag {
        background-color: #fff9f0;
      }

      .point-tag {
        background-color: #fffbe6;
      }
    }

    // ================ 修改：订单选项栏样式 ================
    .order-tabs {
      display: flex;
      justify-content: space-between;
      padding: 30rpx 0 20rpx;
      border-top: 2rpx solid #f5f5f5;
      border-bottom: 2rpx solid #f5f5f5;
      margin-top: 30rpx;
      flex-wrap: nowrap; /* 防止换行 */
      overflow-x: auto; /* 防止内容溢出 */
    }

    .order-tab-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      position: relative;
      flex: 1;
      min-width: 0; /* 防止内容过长 */
      padding: 0 8rpx; /* 添加内边距 */

      .icon-wrapper {
        position: relative;
        display: inline-block; /* 确保容器大小与内容一致 */
        margin-bottom: 12rpx;

        .tab-icon {
          width: 44rpx;
          height: 44rpx;
          transition: transform 0.2s ease;
        }

        .badge {
          position: absolute;
          top: -15rpx;
          right: -15rpx;
          background: #FF5252;
          color: #fff;
          font-size: 20rpx;
          min-width: 25rpx;
          height: 29rpx;
          border-radius: 12rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          text-align: center;
          padding: 0 6rpx;
          font-weight: 600;
          box-shadow: 0 2rpx 6rpx rgba(255, 82, 82, 0.3);
          z-index: 10;

          /* 处理两位数的情况 */
          &:global(.double-digit) {
            min-width: 32rpx;
            right: -10rpx;
          }
        }
      }

      .tab-text {
        font-size: 24rpx;
        color: #333;
        font-weight: 500;
        white-space: nowrap; /* 防止文本换行 */
        text-align: center;
      }

      &:active {
        .tab-icon {
          transform: scale(0.95);
        }
      }
    }
  }

  // 功能列表
  .func-list {
    padding: 0 30rpx;

    // 功能组（每组之间有间距）
    .func-group {
      background-color: #fff;
      border-radius: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      margin-bottom: 20rpx;

      // 功能项
      .func-item {
        display: flex;
        align-items: center;
        padding: 26rpx 30rpx;
        border-bottom: 2rpx solid #f5f5f5;

        &:last-child {
          border-bottom: none;
        }

        .func-icon {
          margin-right: 24rpx;
        }

        .func-text {
          font-size: 30rpx;
          color: #333;
          flex: 1;
        }

        .right-icon {
          opacity: 0.6;
        }
      }
    }

    // 退出登录项
    .logout-item {
      background-color: #fff;
      border-radius: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
      display: flex;
      align-items: center;
      padding: 26rpx 30rpx;
      transition: all 0.2s ease;

      &:active {
        background-color: #f8f8f8;
      }

      .func-icon {
        margin-right: 24rpx;
      }

      .func-text {
        font-size: 30rpx;
        flex: 1;
      }
    }
  }
}
</style>