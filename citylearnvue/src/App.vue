<template>
  <el-config-provider>
    <div class="app-container">
      <el-container>
        <el-header>
          <div class="header-content">
            <h1>城市能源规划系统</h1>
          </div>
        </el-header>
        
        <el-container class="main-container">
          <el-aside width="220px">
            <el-menu
              v-model="activeIndex"
              class="el-menu-vertical"
              @select="handleMenuSelect">
              <el-menu-item index="1">
                <el-icon><DataLine /></el-icon>
                <span>能源消耗统计</span>
              </el-menu-item>
              <el-menu-item index="2">
                <el-icon><TrendCharts /></el-icon>
                <span>能源趋势分析</span>
              </el-menu-item>
              <el-menu-item index="3">
                <el-icon><MapLocation /></el-icon>
                <span>区域能源分布</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          
          <el-main>
            <div class="main-content">
              <component :is="currentComponent" />
            </div>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref } from 'vue'
import { DataLine, TrendCharts, MapLocation } from '@element-plus/icons-vue'
import EnergyConsumption from './components/EnergyConsumption.vue'
import EnergyTrend from './components/EnergyTrend.vue'
import EnergyDistribution from './components/EnergyDistribution.vue'

const activeIndex = ref('1')
const currentComponent = ref(EnergyConsumption)

const handleMenuSelect = (index) => {
  switch(index) {
    case '1':
      currentComponent.value = EnergyConsumption
      break
    case '2':
      currentComponent.value = EnergyTrend
      break
    case '3':
      currentComponent.value = EnergyDistribution
      break
  }
}
</script>

<style>
.app-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.el-header {
  background-color: #409EFF;
  color: white;
  padding: 0;
  z-index: 1000;
}

.header-content {
  height: 60px;
  display: flex;
  align-items: left;
  padding: 0 24px;
  width: 100%;
}

.main-container {
  flex: 1;
  min-height: 0;
  display: flex;
}

.el-aside {
  background-color: #fff;
  border-right: solid 1px #e6e6e6;
  width: 220px !important;
  z-index: 900;
  margin-left: 0;
  position: absolute;
  left: 0;
}

.el-menu-vertical {
  height: calc(100vh - 60px);
  width: 220px;
  margin-left: 0;
}

.el-main {
  padding: 0;
  overflow: auto;
  flex: 1;
  background-color: #fff;
  margin: 0;
}

.main-content {
  width: 100%;
  min-height: calc(100vh - 60px);
  overflow-y: auto;
  padding: 24px;
  margin: 0;
}

/* 重置一些基础样式 */
body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
  overflow: hidden;
}

h1 {
  margin: 0;
  font-size: 20px;
}
</style>
