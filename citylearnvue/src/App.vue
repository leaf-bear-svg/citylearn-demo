<template>
  <div id="app">
    <el-container style="height: 100vh;">
      <el-aside width="200px" style="background-color: #545c64">
        <el-menu
          default-active="dataList"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          @select="handleMenuSelect">
          <el-menu-item index="dataList">
            <i class="el-icon-menu"></i>
            <span>原始数据</span>
          </el-menu-item>
          <el-menu-item index="kpiList">
            <i class="el-icon-document"></i>
            <span>指标数据</span>
          </el-menu-item>
          <el-menu-item index="codeEditor">
            <i class="el-icon-edit"></i>
            <span>代码编辑器</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main>
        <component :is="currentComponent"/>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import DataList from './components/DataList.vue'
import KpiList from './components/KpiList.vue'
import MonacoEditor from './components/MonacoEditor.vue'

export default {
  name: 'App',
  components: {
    DataList,
    KpiList,
    MonacoEditor
  },
  data() {
    return {
      currentComponent: 'DataList'
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.currentComponent = index === 'dataList' ? 'DataList' : 
                            index === 'kpiList' ? 'KpiList' : 'MonacoEditor';
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
.el-menu {
  border-right: none;
}
</style>
