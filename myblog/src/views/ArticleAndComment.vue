<script setup>
import Top from "@/components/Top.vue";
import { useRoute } from 'vue-router'
import {ref,computed, inject, reactive} from 'vue'
import { ElMessageBox } from 'element-plus'
import Comment from "@/components/Comment.vue"


const route = useRoute()//可以通过route获得跳转过来时传递的参数（文章id）
const axios = inject('axios')

let articleAndComment = reactive({   //响应式对象
  "article":{"content": ""} ,
  "comments": []
  })

//添加评论
const commentContent = ref('')
function submit(){
  axios({
    method: 'post',
    url: '/api/comment/insert',
    data: {
      "articleId":route.params.articleId,
      "content":commentContent.value
    },
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      articleAndComment.comments.unshift(response.data.map.comment);
      //element.plus的MessageBox消息弹框
      ElMessageBox.alert(response.data.msg, '结果',{
        confirmButtonText: '确定',
        callback:( action )=>{//回调函数，用户点击确定后会被调用
          commentContent.value=" "//清除评论
      }
      })
    }else{
      ElMessageBox.alert(response.data.msg, '结果')
      }
  }).catch((error) => {//请求失败返回的数据
    ElMessageBox.alert("未登录用户无法评论！", '结果')
  })
}


let pageParams = { "page":1,"rows":2 }//分页参数
//打开新闻时，获取新闻和第一条评论
axios({
  method: 'post',
  url: '/api/article/getArticleAndFirstPageCommentByArticleId?articleId='
        +route.params.articleId, //使用查询字符串传递参数
  data: pageParams,//传递一个对象，服务端使用@RequestBody接收
  timeout: 3000000,
  withCredentials: true
}).then((response) => {
  if(response.data.success){
    if(response.data.map.article!=null){
      articleAndComment.article=response.data.map.article
      articleAndComment.comments=response.data.map.comments
    }else{
      ElMessageBox.alert('无文章！', '结果')
    }
  }else{
    ElMessageBox.alert(articleAndComment.msg, '结果')
  }
})


//分页展示评论
const noMore = ref(false)//是否还有评论
const loading = ref(false)//是否正在加载评论
//计算属性disabled的值由loading和noMore决定
const disabled = computed(()=>loading.value || noMore.value)//计算属性disabled：是否阻止加载新的评论

const load = () => {
  loading.value = true//是否还有评论
  pageParams.page++
  axios({//从后端获取一页评论
    method: 'post',
    url: '/api/comment/getAPageCommentByArticleId?articleId='
          +route.params.articleId, //使用查询字符串传递参数
    data: pageParams,//传递一个对象，服务端使用@RequestBody接收
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      let comments = response.data.map.comments
      if(comments != null && comments.length > 0){
        for (let index = 0; index < comments.length; index++)
          articleAndComment.comments.push(comments[index]);//追加评论到已有评论的末尾
      }else if(comments == null || comments.length == 0)
        noMore.value = true//设置全部评论已加载完毕，从而disabled为真，
    }else
      ElMessageBox.alert(articleAndComment.msg, '结果')

    loading.value = false//本次加载完毕，从而loading为假，恢复无线滚动功能
  }).catch((error) => {//请求失败返回的数据
    ElMessageBox.alert("系统错误！",'结果')
    loading.value=false//本次加载完毕，从而loading为假，恢复无线滚动功能
  })
}
</script>
<template>
  <el-affix>
    <Top />
  </el-affix>

  <el-row>
    <el-col :span="14" :offset="5">
      <!-- v-html表示div内部的html代码为其绑定的articleAndComment.article.content -->
      <div v-html="articleAndComment.article.content"></div>
    </el-col>
  </el-row>

  <el-row style="background-color:#f7f7f7;">
    <el-col :span="14" :offset="5">
      <ul v-infinite-scroll="load":infinite-scroll-disabled="disabled" class="infinite-list">
        <li class="infinite-list-item">
          <el-row>
            <el-col>
              <el-input v-model="commentContent" :autosize="{minRows: 4}"
                        type="textarea" placeholder="评论不能为空！" minlength="2" />
            </el-col>
          </el-row>
          <el-row justify="end">
            <el-col :xs="8" :sm="6" :md="4">
              <el-button @click="submit" type="primary" round style="margin-top: 5px;">提交评论</el-button>
            </el-col>
          </el-row>
        </li>

        <li v-for="comment in articleAndComment.comments" :key="comment.id" class="infinite-list-item">
          <Comment :comment="comment"></Comment>
        </li>
      </ul>
      <p v-if="loading">加载中...</p>
      <p v-if="noMore" class="end-comment">已显示全部评论</p>
      <p></p>
    </el-col>
  </el-row>
</template>

<style scoped>
.infinite-list{
  padding:0;
  margin:0;
  list-style:none;
}

.infinite-list-item{
  background-color: #fff;
  margin-bottom:10px;
  margin-top:10px;
  padding:20px;
}
</style>