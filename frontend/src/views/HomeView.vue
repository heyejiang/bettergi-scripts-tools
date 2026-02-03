<template>
  <div class="home">
    <div class="welcome-card">
      <img class="logo" src="@assets/logo.jpg" alt="Logo" />
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
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  background: linear-gradient(135deg, #74ebd5, #acb6e5);
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.welcome-card {
  background: #fff;
  padding: 40px 60px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  text-align: center;
  max-width: 400px;
  width: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.welcome-card .logo {
  width: 80px;
  margin-bottom: 20px;
}

.welcome-card h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.welcome-card p {
  margin: 10px 0 30px;
  color: #666;
  font-size: 16px;
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.btn {
  padding: 10px 20px;
  font-size: 14px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn.primary {
  background-color: #4cafef;
  color: white;
}

.btn.primary:hover {
  background-color: #3b90d9;
}

.btn.secondary {
  background-color: #e0e0e0;
  color: #333;
}

.btn.secondary:hover {
  background-color: #cfcfcf;
}
</style>
