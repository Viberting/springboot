<script setup>
import {dateFormat} from "@/js/tool.js"   //引入tool.js中的dateFormat函数
import {ref} from 'vue'
const props = defineProps(['article'])    //定义组件属性
const size = ref(40)  //el-space组件中使用的间距
</script>
<template>
  <el-row>
    <!-- 响应式设计，浏览器窗口变小时（sm），占用一行（24），md或更大窗口时，占一行的11/24 -->
    <el-col :sm="24" :md="11">    
      <el-image :src="props.article.thumbnail" />
    </el-col>
    
    <el-col :sm="0" :md="1"></el-col>
    <el-col :sm="24" :md="12">
      <el-row align="middle">
        <el-col>
          <el-space :size="size">
            <span id="categories" class="categorie-height" v-html="props.article.categories"></span>
            <span class="categorie-height" v-html="'发布于' + dateFormat(props.article.created,'yyyy-MM-dd')"></span>
          </el-space>
        </el-col>
      </el-row>

      <el-row align="middle">
        <el-col>
          <router-link :to="{path:'/article_comment/'+props.article.id}"
                  style="text-decoration:none;">
            <span class="title" v-html="props.article.title"></span>
          </router-link>
        </el-col>
      </el-row>
      
      <el-row align="middle">
        <el-col>
          <div style="width: 100%;
              display: -webkit-box;
              -webkit-line-clamp: 5;  /* 最多显示5行 */
              -webkit-box-orient: vertical;
              overflow: hidden;
              text-overflow: ellipsis;">
            <span v-html="props.article.content.substring(0,150)"></span>
            <span v-if="props.article.content.length > 150">...</span>
          </div>
        </el-col>
      </el-row>
    </el-col>
    <el-col :span="1"></el-col>
  </el-row>
  <el-divider />
</template>

<style scoped>
/* #表示元素选择器，hover表示鼠标在其上时 */
.categorie:hover{color: #10d07a;}

.categorie-height{line-height: 40px;}

.title{
  color: #0f9ae0;
  font-size: 20px;
  line-height: 40px;
}
</style>