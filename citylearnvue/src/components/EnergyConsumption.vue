<template>
  <div class="energy-consumption">
    <div class="chart-section">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <h3>城市能源消耗统计</h3>
            <el-select v-model="selectedPeriod" placeholder="选择时间周期">
              <el-option label="日" value="daily" />
              <el-option label="周" value="weekly" />
              <el-option label="月" value="monthly" />
            </el-select>
          </div>
        </template>
        <div ref="chartRef" class="chart-container"></div>
      </el-card>
    </div>

    <div class="statistics-section">
      <el-row :gutter="24" class="statistics-cards">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Lightning /></el-icon>
                电力消耗
              </div>
            </template>
            <div class="statistics-value">
              {{ electricityConsumption }} kWh
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Van /></el-icon>
                燃气消耗
              </div>
            </template>
            <div class="statistics-value">
              {{ gasConsumption }} m³
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <el-icon><Bowl /></el-icon>
                水资源消耗
              </div>
            </template>
            <div class="statistics-value">
              {{ waterConsumption }} m³
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Lightning, Van, Bowl } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
const selectedPeriod = ref('daily')
const electricityConsumption = ref(1234)
const gasConsumption = ref(567)
const waterConsumption = ref(890)

let chart = null

const initChart = () => {
  if (chart) {
    chart.dispose()
  }

  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['电力', '燃气', '水资源']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '电力',
        type: 'bar',
        data: [320, 332, 301, 334, 390, 330]
      },
      {
        name: '燃气',
        type: 'bar',
        data: [220, 182, 191, 234, 290, 330]
      },
      {
        name: '水资源',
        type: 'bar',
        data: [150, 232, 201, 154, 190, 330]
      }
    ]
  }

  chart.setOption(option)
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => {
    chart && chart.resize()
  })
})
</script>

<style scoped>
.energy-consumption {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-section {
  flex: 1;
  min-height: 0;
}

.chart-card {
  height: 100%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.chart-container {
  flex: 1;
  min-height: 500px;
  padding: 24px;
}

.statistics-section {
  min-height: 160px;
}

.statistics-cards {
  height: 100%;
}

.statistics-cards .el-card {
  height: 100%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.statistics-cards .el-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.statistics-value {
  font-size: 28px;
  text-align: center;
  color: #409EFF;
  margin: 24px 0;
  font-weight: 500;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 20px;
}
</style>