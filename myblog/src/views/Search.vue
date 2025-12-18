<script setup>
import Top from "@/components/Top.vue";
import { reactive, inject, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus' 
import { useStore } from '@/stores/my'

// 状态与依赖
const store = useStore()

// 分页条件查询文章的参数（本地副本，可和 store.searchState 同步）
const data = reactive({
  "articleCondition": {"title": "", "startDate": "", "endDate": ""},
  "pageParams": { "page" : 1, "rows":5,"total":0}
})
//查询结果（本地副本）
let myData = reactive({
  "articleVOs": [],
  "pageParams": { "page": 1, "rows": 5, "total": 0 }
})

const axios = inject('axios')

// 当从文章详情返回到搜索页时，需要从 store 中恢复之前的查询状态（而不是重新查询）
onMounted(() => {
  const s = store.searchState
  if (s && s.articleVOs && s.articleVOs.length > 0) {
    // 恢复查询条件
    Object.assign(data.articleCondition, s.articleCondition)
    Object.assign(data.pageParams, s.pageParams)
    // 恢复查询结果
    myData.articleVOs = s.articleVOs
    myData.pageParams = s.pageParams
  }
})

function search(resetPage = false){ 
  if(resetPage){
    data.pageParams.page = 1
  }
  axios({
    method:'post',
    url:'/api/article/articleSearch',
    data:data
  }).then((response) => {
    if(response.data.success){
        myData.articleVOs=response.data.map.articleVOs//符合条件的文章信息
        myData.pageParams=response.data.map.pageParams//返回结果中包含total
        // 更新全局 store 的 searchState，用于在路由返回时恢复
        store.searchState.articleVOs = response.data.map.articleVOs
        store.searchState.pageParams = response.data.map.pageParams
        store.searchState.articleCondition = { ...data.articleCondition }
      }else{
        ElMessageBox.alert(response.data.msg,'结果')
      }
  }).catch((error) => { //请求失败返回的数据
    ElMessageBox.alert("系统错误！",'结果')
  });
}

function handleCurrentChange(newPage){
  data.pageParams.page = newPage
  search(false)
}

function handleSizeChange(newSize){
  data.pageParams.rows = newSize
  data.pageParams.page = 1
  search(false)
}
</script>

<template>
  <el-affix><Top/></el-affix>
  <el-row justify="center" style="margin-top:30px"> 
    <el-col :span="12">
      <el-input v-model="data.articleCondition.title"
          placeholder="文章标题" title="文章标题" clearable />
    </el-col>
  </el-row>
  <el-row justify="center" style="margin-top:15px">
    <el-col :span="12">
      <el-space :size="40">
        <el-date-picker value-format="YYYY-MM-DD"
          v-model="data.articleCondition.startDate"
          type="date" placeholder="起始日期" title="起始日期"/>
        <el-date-picker value-format="YYYY-MM-DD"
          v-model="data.articleCondition.endDate"
          type="date" placeholder="结束日期" title="结束日期"/>
        <el-button type="primary" @click="search(true)">开始查询</el-button>
      </el-space>
    </el-col>
  </el-row>
  <el-row justify="center">
    <el-col :span="20">
      <h4 style="margin:0 0 10px 0; text-align:left;">查询结果</h4>
      <el-table :data="myData.articleVOs" stripe border style="width:100%">
        <el-table-column prop="categories" label="所属分类" width="150" />
        <el-table-column label="文章标题" width="800" >
        <template #default="scope">
          <!-- text-decoration:none是清除超链接下划线  scope.row表示当前行数据，通过它可以访问title等属性-->
          <!-- 跳转到文章详情页面 -->
          <router-link :to="{path: '/article_comment/'+scope.row.id }" 
              style="text-decoration:none;">
            {{ scope.row.title }}
          </router-link>
        </template>
        </el-table-column>
        <el-table-column prop="created" label="发布时间" width="170"/>
      </el-table>
      <div style="margin-top:12px; text-align:right;">
        <el-pagination
          v-model:current-page="data.pageParams.page"
          v-model:page-size="data.pageParams.rows"
          :page-sizes="[5,10,20]"
          layout="sizes, prev, pager, next, jumper"
          :total="myData.pageParams ? myData.pageParams.total : 0"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-col>
  </el-row>
</template>
<style scoped>

</style>