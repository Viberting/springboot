<script setup>
import { reactive,watch } from 'vue'
const props = defineProps(['articleVOs'])    //定义组件属性
let rankings = reactive([])  //响应式对象

// 侦听某个对象：当对象发生变化时，vue会自动调用此方法，且在此方法中可以获得被侦听对象变化前后的不同值
watch(()=>props.articleVOs,(newValue,oldValue)=>{
    rankings.length=0
    for(let index=0;index<newValue.length;index++){
     rankings.push((index+1)+'、'+newValue[index].title+"("+newValue[index].hits+")")
    }
  }
)
</script>
<template>
  <div class="read-ranking-card">
    <h3 class="card-title">阅读排行榜</h3>
    <!-- 循环展示 -->
    <el-row v-for="(ranking, idx) in rankings" :key="idx" class="ranking-item">
      <el-col :span="24">
        {{ ranking }}
      </el-col>
    </el-row>
  </div>
</template>
<style scoped>
.read-ranking-card{
  border:1px solid #e6e6e6;
  border-radius:8px;
  padding:10px 12px;
  background:#ffffff;
}
.card-title{
  margin:0 0 8px 0;
  font-size:16px;
  color:#333;
}
.ranking-item{
  padding:8px 0;
}
.ranking-item:not(:last-child){
  border-bottom:1px solid #f3f3f3; /* 内横线 */
}

a:hover{
  color: #10d07a;
}

.title{
  color:black;
  font-size: 15px;
}

</style>