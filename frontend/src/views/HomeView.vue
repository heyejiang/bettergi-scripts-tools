<template>
  <div class="home">
    <div class="welcome-card">
      <img class="logo" src="@assets/logo.svg" alt="Logo" />
      <h2 class="title">{{currentRoute.meta.title||'HOME'}}</h2>
      <p class="subtitle">欢迎使用扩展工具</p>

      <!-- 外层结构遍历 -->
      <div v-for="group in featureGroup" :key="group.title" class="feature-section">
        <h3 class="section-title" v-if="group.children.length > 0">{{ group.title }}</h3>
        <div class="feature-container">
          <!-- 左侧功能列表 -->
          <div class="feature-column">
            <div
              v-for="item in getItemsByPosition(group.children, 'left')"
              :key="item.id"
              :class="['feature-item', getItemClass(item)]"
            >
              <span class="icon">{{ getIcon(item) }}</span>
              <button class="name" @click="togo(item)">{{ item.name }}</button>
            </div>
          </div>

          <!-- 右侧功能列表 -->
          <div class="feature-column">
            <div
              v-for="item in getItemsByPosition(group.children, 'right')"
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
  </div>
</template>



<!--<template>
  <div class="home">
    <div class="welcome-card">
      <img class="logo" src="@assets/logo.svg" alt="Logo"/>
      <h2 class="title">HOME</h2>
      <p class="subtitle">欢迎使用扩展工具</p>

      &lt;!&ndash; 遍历外层结构 &ndash;&gt;
      <div v-for="group in featureGroup" :key="group.title" class="feature-group">
        <h3 class="group-title" v-if="group.children.length>0">{{ group.title }}</h3>
        <div class="feature-wrapper">
          &lt;!&ndash; 遍历子项 &ndash;&gt;
          <div class="feature-list-left"
              v-for="item in getItemsByPosition(group.children,'left')"
              :key="item.id"
              :class="['feature-item', getItemClass(item)]"
          >
            <span class="icon">{{ getIcon(item) }}</span>
            <button class="name" @click="togo(item)">{{ item.name }}</button>
          </div>

          <div class="feature-list-right"
              v-for="item in getItemsByPosition(group.children,'right')"
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
</template>-->


<script>
import {ref, onMounted} from "vue";
import router from "@router/router.js";
const currentRoute=ref(router.currentRoute)
export default {
  name: 'HomeView',
  setup() {

    // 统一管理所有功能项
    const featureGroup = ref([]);
    const list = [
      // {isLink: true, name: 'API 调试链接', value: 'API 调试链接'},
      {isSwagger: true, name: 'Swagger 文档入口', value: 'doc.html'},
      // {isRote: true, name: '路由管理面板', value: '路由管理面板'},
    ]
    let index = 1
    let initJson = {
      title: '功能列表',
      children: []
    }
    list.forEach(item => {
      initJson.children.push({
        id: index,
        position: index % 2 === 1 ? "left" : "right",
        isRote: item.isRote,
        isLink: item.isLink,
        isSwagger: item.isSwagger,
        name: item.name,
        value: item.value
      });
      index++
    })
    featureGroup.value.push(initJson);
    onMounted(() => {
      let index = 1
      let routerJson = {
        title: '路由功能列表',
        children: []
      }

      router.getRoutes().filter(route => route.name !== 'home').forEach(route => {
        routerJson.children.push({
          id: index,
          position: index % 2 === 1 ? "left" : "right",
          isRote: true,
          name: route?.meta?.title,
          value: route.path
        });
        index++
      });
      featureGroup.value.push(routerJson);
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
    const getItemsByPosition = (featureGroup,position) => {
      return featureGroup.filter((item) => item.position === position);
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

    return {
      currentRoute,
      featureGroup,
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
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #a1c4fd, #c2e9fb);
  background-size: cover;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* 中间卡片 */
.welcome-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 50px 70px;
  border-radius: 25px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  text-align: center;
  max-width: 600px;
  width: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

/* Logo 圆角 */
.logo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  margin-bottom: 25px;
  border: 3px solid #6a89cc;
}

.title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #2c3e50;
}

.subtitle {
  font-size: 18px;
  color: #7f8c8d;
  margin-bottom: 40px;
}

/* 功能区域 */
.feature-section {
  margin-top: 30px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #34495e;
}

.feature-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.feature-column {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 12px;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.icon {
  font-size: 20px;
  margin-right: 15px;
}

.name {
  border: none;
  background: transparent;
  font-size: 16px;
  color: #3498db;
  cursor: pointer;
  font-weight: 500;
}

/* 类型区分 */
.link-item {
  background: #e8f8f5;
  color: #27ae60;
}

.swagger-item {
  background: #fef9e7;
  color: #f39c12;
}

.routes-item {
  background: #fadbd8;
  color: #e74c3c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feature-container {
    grid-template-columns: 1fr;
  }

  .welcome-card {
    padding: 30px 40px;
  }

  .title {
    font-size: 28px;
  }

  .subtitle {
    font-size: 16px;
  }
}
</style>


