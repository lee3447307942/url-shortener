<template>
  <div class="home">
    <div class="hero">
      <h1>短链服务</h1>
      <p class="subtitle">简单、快速、可靠的短链接生成平台</p>
      <div class="actions">
        <router-link to="/register">
          <el-button type="primary" size="large">免费注册</el-button>
        </router-link>
        <router-link to="/login">
          <el-button size="large">登录</el-button>
        </router-link>
      </div>
    </div>

    <div class="features">
      <el-row :gutter="24">
        <el-col :span="8">
          <el-card>
            <h3>快速生成</h3>
            <p>一键生成短链接，支持自定义短码</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <h3>数据统计</h3>
            <p>实时查看点击数据，了解链接表现</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <h3>过期控制</h3>
            <p>灵活设置链接有效期，自动失效</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 1000px;
  margin: 0 auto;
  padding: 60px 24px;
}
.hero {
  text-align: center;
  margin-bottom: 60px;
}
.hero h1 {
  font-size: 48px;
  margin-bottom: 16px;
  color: #333;
}
.subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 32px;
}
.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
.features h3 {
  margin-bottom: 8px;
}
.features p {
  color: #666;
}
</style>
