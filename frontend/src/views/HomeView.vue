<template>
  <div class="home">
    <div class="welcome-card">

      <img class="logo" src="@assets/logo.svg" alt="Logo"/>
      <h2 class="title">HOME</h2>
      <p class="subtitle">欢迎使用扩展工具</p>

      <!-- 左侧功能列表 -->
      <div class="feature-list">
        <h3 class="feature-title">左侧功能</h3>
        <div class="feature-grid">
          <div
              v-for="item in getItemsByPosition('left')"
              :key="item.id"
              :class="['feature-item', getItemClass(item)]"
          >
            <span class="icon">{{ getIcon(item) }}</span>
            <button class="name" @click="togo(item)">{{ item.name }}</button>
          </div>
        </div>
      </div>

      <!-- 右侧功能列表 -->
      <div class="feature-list">
        <h3 class="feature-title">右侧功能</h3>
        <div class="feature-grid">
          <div
              v-for="item in getItemsByPosition('right')"
              :key="item.id"
              :class="['feature-item', getItemClass(item)]"
          >
            <span class="icon">{{ getIcon(item) }}</span>
            <button class="name" @click="togo(item)">{{ item.name }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import {ref, onMounted} from "vue";
import router from "@router/router.js";

export default {
  name: 'HomeView',
  setup() {
    // 统一管理所有功能项
    const featureItems = ref([]);
    const list = [
      {isLink: true, name: 'API 调试链接', value: 'API 调试链接'},
      {isSwagger: true, name: 'Swagger 文档入口', value: 'doc.html'},
      {isRote: true, name: '路由管理面板', value: '路由管理面板'},
    ]
    let index = 1
    list.forEach(item => {
      featureItems.value.push({
        id:index,
        position: index % 2 === 1 ? "left" : "right",
        isRote: item.isRote,
        isLink: item.isLink,
        isSwagger: item.isSwagger,
        name: item.name,
        value: item.value
      });
      index++
    })
    onMounted(() => {
      router.getRoutes().filter(route => route.name !== 'home').forEach(route => {
        featureItems.value.push({
          id:index,
          position: index % 2 === 1 ? "left" : "right",
          isRote: true,
          name: route?.meta?.title,
          value: route.path
        });
        index++
      });
    });

    // 获取图标
    const getIcon = (item) => {
      return item.isLink ? "🔗" : item.isSwagger ? "📖" : item.isRote ? "🛤️" : "";
    };
    // 获取样式类
    const getItemClass = (item) => {
      return {
        "link-item": item.isLink,
        "swagger-item": item.isSwagger,
        "routes-item": item.isRote,
      };
    };
    // 根据 position 分组
    const getItemsByPosition = (position) => {
      return featureItems.value.filter((item) => item.position === position);
    };

    // 点击跳转
    const togo = async (item) => {
      if (item?.isRote) {
        try {
          await router.push(item.value);
        } catch (error) {
          console.error('路由跳转失败:', error);
        }
      } else if (item?.isSwagger) {
        const basePath = import.meta.env.VITE_BASE_PATH || '/bgi/';
        window.open(`${basePath}${item.value}`, '_blank');
      } else if (item?.isLink) {
        window.open(item.value, '_blank');
      }
    };

    // const goFeature1 = () => {
    //   alert('跳转到功能一');
    // };
    //
    // const goFeature2 = () => {
    //   alert('跳转到功能二');
    // };

    return {
      featureItems,
      togo,
      getIcon,
      getItemClass,
      getItemsByPosition
      // goFeature1,
      // goFeature2
    };
  }
};

</script>

<style scoped>
/* 页面全屏背景 */
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 整个视口高度 */
  width: 100vw; /* 整个视口宽度 */
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

.title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 30px;
}

/*//.feature-list {
//  text-align: left;
//}*/

.feature-list {
  display: inline-block;
  width: 48%;
  vertical-align: top;
  margin-right: 2%;
}

.feature-list:last-child {
  margin-right: 0;
}


.feature-title {
  font-size: 20px;
  font-weight: 500;
  margin-bottom: 15px;
  color: #333;
}

/* 网格布局 */
.feature-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  background: #e0e7ff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1 1 45%; /* 自适应两列布局 */
  max-width: 48%;
}

.feature-item .icon {
  margin-right: 10px;
  font-size: 18px;
}

.feature-item .name {
  border: none;
  background: transparent;
  font-size: 14px;
  color: #1e40af;
  cursor: pointer;
}

/* 悬停效果 */
.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 类型区分 */
.link-item {
  background: #d1fae5;
  color: #065f46;
}

.link-item:hover {
  background: #a7f3d0;
}

.swagger-item {
  background: #fef3c7;
  color: #92400e;
}

.swagger-item:hover {
  background: #fde68a;
}

.routes-item {
  background: #fee2e2;
  color: #b91c1c;
}

.routes-item:hover {
  background: #fca5a5;
}
</style>
