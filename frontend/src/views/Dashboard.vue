<template>
  <div class="container">
    <h1>我的短链</h1>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab">
      <!-- 创建短链 -->
      <el-tab-pane label="创建短链" name="create">
        <el-card>
          <el-form :model="form" label-width="80px">
            <el-form-item label="原始链接">
              <el-input v-model="form.url" placeholder="https://example.com/very/long/url" @blur="handleSafeCheck" />
              <div v-if="safeResult" class="safe-tip" :class="safeResult.risk">
                <el-icon><Warning /></el-icon>
                {{ safeResult.reason }}
              </div>
            </el-form-item>
            <el-form-item label="自定义码">
              <el-input v-model="form.customCode" placeholder="留空则随机生成" />
            </el-form-item>
            <el-form-item label="过期天数">
              <el-input-number v-model="form.expireDays" :min="0" :max="365" />
              <span style="margin-left: 8px; color: #999;">0 = 永不过期</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCreate" :loading="creating">生成短链</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 批量生成 -->
      <el-tab-pane label="批量生成" name="batch">
        <el-card>
          <el-input
            v-model="batchUrls"
            type="textarea"
            :rows="8"
            placeholder="每行一个URL，例如：
https://example.com/page1
https://example.com/page2
https://example.com/page3"
          />
          <el-button type="primary" @click="handleBatchCreate" :loading="batchLoading" style="margin-top: 12px">
            批量生成
          </el-button>

          <el-table v-if="batchResults.length" :data="batchResults" style="margin-top: 16px;" stripe>
            <el-table-column prop="originalUrl" label="原始链接" show-overflow-tooltip />
            <el-table-column prop="shortUrl" label="短链">
              <template #default="{ row }">
                <el-link @click="copyText(row.shortUrl)">{{ row.shortUrl }}</el-link>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 我的链接 -->
      <el-tab-pane label="我的链接" name="links">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>链接列表（{{ links.length }}）</span>
              <el-button size="small" @click="loadLinks" :loading="loading">刷新</el-button>
            </div>
          </template>

          <el-table :data="links" stripe v-loading="loading">
            <el-table-column prop="shortCode" label="短码" width="120">
              <template #default="{ row }">
                <el-link @click="copyLink(row.shortCode)">{{ row.shortCode }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="originalUrl" label="原始链接" show-overflow-tooltip />
            <el-table-column prop="clickCount" label="点击数" width="100" align="center" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column prop="expireAt" label="过期时间" width="180">
              <template #default="{ row }">
                {{ row.expireAt || '永不过期' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="viewAnalytics(row.shortCode)">统计</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 域名设置 -->
      <el-tab-pane label="域名设置" name="domain">
        <el-card>
          <h3>自定义域名</h3>
          <p class="tip">设置后，生成的短链将使用你的域名</p>
          <el-form :model="domainForm" label-width="100px" style="max-width: 500px;">
            <el-form-item label="当前域名">
              <el-tag>{{ userDomain || '未设置（使用默认域名）' }}</el-tag>
            </el-form-item>
            <el-form-item label="新域名">
              <el-input v-model="domainForm.customDomain" placeholder="例如: s.example.com" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveDomain" :loading="domainLoading">保存</el-button>
              <el-button @click="handleClearDomain" v-if="userDomain">清除</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 统计弹窗 -->
    <el-dialog v-model="analyticsVisible" title="链接统计" width="800px">
      <div v-if="analytics" class="analytics-content">
        <el-row :gutter="16" style="margin-bottom: 20px;">
          <el-col :span="8">
            <el-statistic title="总点击数" :value="analytics.totalClicks" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="近30天点击" :value="analytics.periodClicks" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="短码" :value="analytics.shortCode" />
          </el-col>
        </el-row>

        <!-- 点击趋势图 -->
        <div ref="trendChart" class="chart-container"></div>

        <!-- 浏览器和来源分布 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <div ref="browserChart" class="chart-container"></div>
          </el-col>
          <el-col :span="12">
            <div ref="referrerChart" class="chart-container"></div>
          </el-col>
        </el-row>

        <!-- 最近点击 -->
        <h4 style="margin-top: 20px;">最近点击</h4>
        <el-table :data="analytics.recentClicks" stripe size="small">
          <el-table-column prop="clickedAt" label="时间" width="180" />
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="browser" label="浏览器" width="100" />
          <el-table-column prop="os" label="系统" width="100" />
          <el-table-column prop="referer" label="来源" show-overflow-tooltip />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { shorten, batchShorten, getStats, getMyLinks, getAnalytics, checkSafe, updateDomain, getMe } from '../api'

// Tab
const activeTab = ref('create')

// 单个创建
const form = ref({ url: '', customCode: '', expireDays: 0 })
const creating = ref(false)
const safeResult = ref(null)

// 批量创建
const batchUrls = ref('')
const batchLoading = ref(false)
const batchResults = ref([])

// 链接列表
const loading = ref(false)
const links = ref([])

// 统计弹窗
const analyticsVisible = ref(false)
const analytics = ref(null)
const trendChart = ref(null)
const browserChart = ref(null)
const referrerChart = ref(null)

// 域名设置
const domainForm = ref({ customDomain: '' })
const domainLoading = ref(false)
const userDomain = ref('')

// 安全检测
const handleSafeCheck = async () => {
  if (!form.value.url) {
    safeResult.value = null
    return
  }
  try {
    const { data } = await checkSafe(form.value.url)
    safeResult.value = data
  } catch {
    safeResult.value = null
  }
}

// 创建短链
const handleCreate = async () => {
  if (!form.value.url) {
    ElMessage.warning('请输入链接')
    return
  }
  creating.value = true
  try {
    await shorten(form.value.url, form.value.customCode || null, form.value.expireDays || null)
    ElMessage.success('生成成功')
    form.value = { url: '', customCode: '', expireDays: 0 }
    safeResult.value = null
    loadLinks()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '生成失败')
  } finally {
    creating.value = false
  }
}

// 批量创建
const handleBatchCreate = async () => {
  const urls = batchUrls.value.split('\n').filter(u => u.trim())
  if (!urls.length) {
    ElMessage.warning('请输入至少一个URL')
    return
  }
  batchLoading.value = true
  try {
    const { data } = await batchShorten(urls)
    batchResults.value = data.success
    ElMessage.success(`成功生成 ${data.total} 条短链`)
    loadLinks()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '批量生成失败')
  } finally {
    batchLoading.value = false
  }
}

// 加载链接列表
const loadLinks = async () => {
  loading.value = true
  try {
    const { data } = await getMyLinks()
    links.value = data.links
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 查看统计
const viewAnalytics = async (shortCode) => {
  try {
    const { data } = await getAnalytics(shortCode)
    analytics.value = data
    analyticsVisible.value = true
    await nextTick()
    renderCharts(data)
  } catch {
    ElMessage.error('获取统计失败')
  }
}

// 渲染图表
const renderCharts = (data) => {
  // 点击趋势图
  if (trendChart.value) {
    const chart = echarts.init(trendChart.value)
    const dates = Object.keys(data.dailyClicks).sort()
    const values = dates.map(d => data.dailyClicks[d])
    chart.setOption({
      title: { text: '点击趋势', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{ data: values, type: 'line', smooth: true, areaStyle: { opacity: 0.3 } }]
    })
  }

  // 浏览器分布
  if (browserChart.value && Object.keys(data.browsers).length) {
    const chart = echarts.init(browserChart.value)
    const pieData = Object.entries(data.browsers).map(([name, value]) => ({ name, value }))
    chart.setOption({
      title: { text: '浏览器分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '60%', data: pieData }]
    })
  }

  // 来源分布
  if (referrerChart.value && Object.keys(data.referrers).length) {
    const chart = echarts.init(referrerChart.value)
    const pieData = Object.entries(data.referrers)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([name, value]) => ({ name, value }))
    chart.setOption({
      title: { text: '来源分布', left: 'center' },
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: '60%', data: pieData }]
    })
  }
}

// 域名设置
const loadUserInfo = async () => {
  try {
    const { data } = await getMe()
    userDomain.value = data.user.customDomain || ''
    domainForm.value.customDomain = data.user.customDomain || ''
  } catch {}
}

const handleSaveDomain = async () => {
  domainLoading.value = true
  try {
    await updateDomain(domainForm.value.customDomain || null)
    userDomain.value = domainForm.value.customDomain
    ElMessage.success('域名设置成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '设置失败')
  } finally {
    domainLoading.value = false
  }
}

const handleClearDomain = async () => {
  domainForm.value.customDomain = ''
  await handleSaveDomain()
}

// 复制
const copyLink = (shortCode) => {
  const url = `${window.location.origin}/${shortCode}`
  navigator.clipboard.writeText(url)
  ElMessage.success('已复制')
}

const copyText = (text) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}

onMounted(() => {
  loadLinks()
  loadUserInfo()
})
</script>

<style scoped>
.container { max-width: 1000px; margin: 0 auto; padding: 24px; }
h1 { text-align: center; margin-bottom: 24px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tip { color: #999; font-size: 14px; margin-bottom: 16px; }
.safe-tip {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.safe-tip.low { background: #f0f9ff; color: #409eff; }
.safe-tip.medium { background: #fdf6ec; color: #e6a23c; }
.safe-tip.high { background: #fef0f0; color: #f56c6c; }
.chart-container { height: 300px; margin-bottom: 20px; }
.analytics-content { max-height: 600px; overflow-y: auto; }
</style>
