<template>
  <div class="energy-distribution">
    <h2>区域能源分布</h2>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="date" label="Date" width="180" />
      <el-table-column prop="name" label="Name" width="180" />
      <el-table-column prop="address" label="Address" />
    </el-table>
  </div>
</template>
<script>
export default {
  data() {
    return {
      tableData: []
    }
  },
  created() {
    this.axios.get("/api/web/basedata/getList?buildingId=building1").then((response) => {
      console.log(response.data.data)
     
    })
  }
}
</script>

<style scoped>
.energy-distribution {
  padding: 20px;
}
</style>