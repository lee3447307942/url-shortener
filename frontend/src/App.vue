<template>
  <el-container>
    <el-header class="app-header">
      <div class="header-content">
        <router-link to="/" class="logo">短链服务</router-link>
        <div class="nav-right">
          <template v-if="user">
            <span class="username">{{ user.username }}</span>
            <router-link to="/dashboard">
              <el-button size="small" link>我的短链</el-button>
            </router-link>
            <el-button size="small" link @click="handleLogout">退出</el-button>
          </template>
          <template v-else>
            <router-link to="/login">
              <el-button size="small" link>登录</el-button>
            </router-link>
            <router-link to="/register">
              <el-button size="small" type="primary">注册</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const user = ref(null)

const loadUser = () => {
  const stored = localStorage.getItem('user')
  user.value = stored ? JSON.parse(stored) : null
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  user.value = null
  router.push('/')
}

onMounted(loadUser)
watch(() => route.path, loadUser)
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { background: #f5f5f5; font-family: -apple-system, sans-serif; }
.app-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 16px;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  color: #666;
  font-size: 14px;
}

/* 手机端适配 */
@media (max-width: 768px) {
  .header-content {
    height: 50px;
    padding: 0 12px;
  }
  .logo {
    font-size: 16px;
  }
  .nav-right {
    gap: 8px;
  }
  .username {
    display: none;
  }
}
</style>
