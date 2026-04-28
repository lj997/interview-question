<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <h2>📚 面试题库管理系统</h2>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF">
          <el-menu-item index="/questions">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/browse">
            <el-icon><Search /></el-icon>
            <span>题目浏览</span>
          </el-menu-item>
          <el-menu-item index="/random">
            <el-icon><Promotion /></el-icon>
            <span>随机抽题</span>
          </el-menu-item>
          <el-menu-item index="/wrong">
            <el-icon><Warning /></el-icon>
            <span>错题本</span>
          </el-menu-item>
          <el-menu-item index="/interviews">
            <el-icon><Calendar /></el-icon>
            <span>面试记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document, Search, Promotion, Warning, Calendar } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.app-container {
  height: 100%;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.app-aside {
  background-color: #304156;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.el-menu {
  border-right: none;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.filter-item {
  min-width: 150px;
}

.mastery-0 { color: #f56c6c; }
.mastery-1 { color: #e6a23c; }
.mastery-2 { color: #67c23a; }
.mastery-3 { color: #409eff; }

.difficulty-1 { background: #67c23a; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.difficulty-2 { background: #e6a23c; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.difficulty-3 { background: #f56c6c; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
</style>
