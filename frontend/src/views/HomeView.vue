<template>
  <div class="home">
    <div class="welcome-card">
      <img class="logo" src="@assets/logo.svg" alt="Logo" />
      <h2>HOME</h2>
      <p>欢迎使用扩展工具</p>
      <div class="buttons">
        <button class="btn primary" @click="goFeature1">功能一</button>
        <button class="btn secondary" @click="goFeature2">功能二</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
  methods: {
    goFeature1() {
      alert('跳转到功能一')
    },
    goFeature2() {
      alert('跳转到功能二')
    }
  }
}
</script>

<style scoped>
/* 页面全屏背景 */
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 整个视口高度 */
  width: 100vw;  /* 整个视口宽度 */
  /*background: url('/assets/background.jpg') no-repeat center center; !* 背景图 *!*/
  background: linear-gradient(135deg, #74ebd5, #acb6e5);
  background-size: cover; /* 背景铺满整个屏幕 */
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* 中间卡片 */
.welcome-card {
  background: rgba(255, 255, 255, 0.9); /* 半透明白色 */
  padding: 40px 60px;
  border-radius: 20px; /* 卡片圆角 */
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  text-align: center;
  max-width: 400px;
  width: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.25);
}

/* Logo 圆角 */
.welcome-card .logo {
  width: 80px;
  height: 80px;
  object-fit: cover; /* 保持图片比例 */
  border-radius: 50%; /* 完全圆角 */
  margin-bottom: 20px;
  border: 2px solid #4cafef; /* 可选边框 */
}

.welcome-card h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.welcome-card p {
  margin: 10px 0 0;
  color: #666;
  font-size: 16px;
}
</style>
