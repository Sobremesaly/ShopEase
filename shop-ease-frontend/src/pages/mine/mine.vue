<template>
  <view class="mine-container">
    <!-- 顶部背景图 -->
    <view class="top-bg">
      <up-image src="/static/images/img.png" mode="scaleToFill" class="bg-img" width="100%" height="100%"/>
    </view>

    <!-- 用户信息卡片（悬浮在背景图上） -->
    <view class="user-card">
      <!-- 头像 + 昵称 + 手机号 -->
      <view class="user-info">
        <up-image :src="userInfo.avatar || '/static/avatar/icons10.png'" class="avatar" mode="aspectFill" width="100rpx" height="100rpx"/>
        <view class="info-right">
          <view class="nickname">{{ userInfo.nickname || '未设置昵称' }}</view>
          <view class="phone">
            {{ userInfo.phone ? userInfo.phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3') : '未绑定手机' }}
          </view>
        </view>
        <!-- 编辑按钮 -->
        <view class="edit-btn" @click="goEditUser">
          <up-icon name="/static/mine/icons2.png" size="22rpx" color="#fff" />
          <text class="edit-text">编辑</text>
        </view>
      </view>

      <!-- 功能标签（可选，如会员、积分） -->
      <view class="user-tags" v-if="userInfo.memberLevel">
        <view class="tag member-tag">
          <up-icon type="vip" size="20rpx" color="#ff9f43" />
          <text class="tag-text">会员等级 {{ userInfo.memberLevel }}</text>
        </view>
        <view class="tag point-tag">
          <up-icon type="coins" size="20rpx" color="#ffd166" />
          <text class="tag-text">积分 {{ userInfo.points || 0 }}</text>
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
        <view class="func-item" @click="goBindPhone" v-if="!userInfo.phone">
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
import { ref, onMounted } from 'vue';
import { useUserStore } from '../../stores/user';
import { get } from '../../utils/request';

// 状态管理
const userStore = useUserStore();
const userInfo = ref(userStore.userInfo);
const isLoading = ref(false);

// 页面挂载时获取最新用户信息（避免缓存过期）
onMounted(async () => {
  try {
    isLoading.value = true;
    const res = await get('/sys/user/current'); // 调用后端获取最新信息
    userInfo.value = res.data;
    // 更新Pinia缓存
    userStore.setUserInfo({
      ...userStore,
      userInfo: res.data
    });
  } catch (error) {
    console.error('获取用户信息失败：', error);
    // 失败时仍显示缓存信息
    userInfo.value = userStore.userInfo;
  } finally {
    isLoading.value = false;
  }
});

// 跳转编辑个人信息页
const goEditUser = () => {
  uni.navigateTo({ url: '/pages/mine/edit-user' });
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

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#ff4d4f',
    cancelColor: '#666',
    success: (res) => {
      if (res.confirm) {
        userStore.logout(); // 清空状态和缓存
        uni.reLaunch({ url: '/pages/login/login' }); // 跳登录页
        uni.$u.toast('退出成功');
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
        width: 140rpx;
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
      }
    }

    // 功能标签
    .user-tags {
      display: flex;
      gap: 16rpx;
      flex-wrap: wrap;

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
