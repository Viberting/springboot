<script setup>
import Top from "@/components/Top.vue";
import ArticleHeader from "@/components/ArticleHeader.vue";
import ReadRanking from "@/components/ReadRanking.vue";
import { reactive,inject,ref } from 'vue' 
import { ElMessageBox, } from 'element-plus'
import { onBeforeRouteLeave } from 'vue-router'//引入导航守卫：离开当前vue时调用
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

import imageMeUrl from '@/assets/me.jpg'//引入客户端项目内的图片（非服务器端项目中的图片）
import { useStore } from '@/stores/my'//引入状态
const store = useStore()
const size = ref(20)  //el-space组件中使用的间距
const axios = inject('axios')

//响应式对象
const data = reactive({
  "articles": [],
  "pageParams": { "page" : store.home.page, "rows":5,"total":0},
  "articleVOs": [] 
  })  

//分页查询文章
function getAPage(){
  axios({
    method:'post',
    url:'/api/article/getAPageOfArticle',
    data:data.pageParams
  }).then((response) => {
    if(response.data.success){
      if(response.data.map.articles!=null){
        data.articles=response.data.map.articles
        data.pageParams=response.data.map.pageParams
        console.log('getAPage response pageParams:', response.data.map.pageParams)
        window.scrollTo(0,0)//滚动到顶端
      }else{
        ElMessageBox.alert("无文章！",'结果')
      }
    }else{
      ElMessageBox.alert(response.data.msg,'结果')
    }
  }).catch((error) => { //请求失败返回的数据
    ElMessageBox.alert("系统错误！",'结果')
  })
}

function handleCurrentChange(newPage){
  data.pageParams.page=newPage
  getAPage()
}

//请求数据,打开主页时调用
function getIndexData(){
  axios({
    method: 'post',
    url: '/api/article/getIndexData1',
    data:data.pageParams
  })
      .then((response) => { 
        if(response.data.success){
          data.articles=response.data.map.articles
          data.pageParams=response.data.map.pageParams
          console.log('getIndexData response pageParams:', response.data.map.pageParams)
          data.articleVOs=response.data.map.articleVOs
        }else{
          ElMessageBox.alert(response.data.msg, '结果')
        }
      })
      .catch((error)=>{ //请求失败返回的数据
        ElMessageBox.alert("系统错误！", '结果')
      })
}

getIndexData()

//导航守卫：此函数会在离开当前vue时被自动调用
onBeforeRouteLeave((to,from)=> {
  //如果离开此vue去新闻详情页面，则记录当前vue的页码，否则不记录
  //如果点击"后台管理"，然后再返回主页，主页中分页组件只会显示到第1页，而不会回到主页之前的页码状态
  if(to.fullPath.indexOf("article_comment")>=0){
    store.home.page=data.pageParams.page
  }else{
    store.home.page=1
  }
  return true
})
</script>

<template>
  <el-affix><Top/></el-affix>
  <!-- 垂直对齐方式 -->
  <el-row style="margin-top:40px" align="top">
    <el-col :span="1"></el-col>
    <el-col :span="14">
      <!-- 循环展示 -->
      <ArticleHeader
        v-for="(article, idx) in data.articles"
        :key="article.id || idx"
        :article="article"
      />
      <el-row>
        <el-col>
          <el-pagination v-model:currentPage="data.pageParams.page"
          v-model:page-size="data.pageParams.rows"
          layout="prev,pager,next"
          :total="data.pageParams.total"
          @current-change="handleCurrentChange"
          :pager-count="7"
          />
        </el-col>
      </el-row>
    </el-col>
    <el-col :span="1"></el-col>
    <el-col :span="7">
      <fieldset align="center">
        <legend><h3>CrazyStone</h3></legend>
        <el-image :src="imageMeUrl"/>
        <div style="margin-top:16px;">Java后台开发</div>
        <div style="margin-top:16px;">
          个人博客小站，主要发表关于Java、Spring、Docker等相关文章
        </div>
      </fieldset>
      <!-- fontawesome图标，weibo和github -->
      <fieldset align="center">
        <legend><h3>联系我</h3></legend>
        <el-space :size="size">
          <font-awesome-icon class="icon" icon="fa-brands fa-github" size="lg" border />
          <font-awesome-icon class="icon" icon="fa-brands fa-weibo" size="lg" border />
          <font-awesome-icon class="icon" icon="fa-brands fa-qq" size="lg" border />       
        </el-space>
      </fieldset>
      <!-- 阅读排行组件 -->
      <ReadRanking :articleVOs="data.articleVOs" />
    </el-col>
    <el-col :span="1"></el-col>
  </el-row>
</template>

<style scoped>
/* 鼠标移动到标签上时变换文字颜色 */
.icon:hover {
  color: #10d07a;
}

fieldset {
  /* 设置fieldset的边框颜色、粗度和线壁 */
  border-color: #eee;
  border-width: 1px;
  border-style: solid;
}
</style>
