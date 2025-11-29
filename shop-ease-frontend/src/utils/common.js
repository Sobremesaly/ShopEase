// 跨端提示框
export const showToast = (title, icon = 'none') => {
    uni.showToast({ title, icon, duration: 2000, mask: true });
};