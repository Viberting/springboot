<script setup>
  import { reactive, inject, ref } from 'vue'
  import { ElMessageBox } from 'element-plus'//引入消息框和对话框
  import { Edit,Delete, Iphone } from '@element-plus/icons-vue'//引入图标
  import { useStore } from '@/stores/my'//引入状态管理
  import {nullZeroBlank} from '@/js/tool.js'//引入tool.js中的一个函数，此vue中可调用该函数
  const store = useStore()//可以使用是store访问my.js中的两个状态

  const axios = inject('axios')
  let myData = reactive({
    "articleVOs": [],//空数组
    "pageParams": {"page":1,"rows":4,"total":0}
  })

  function getAPage(){
    if(!nullZeroBlank(store.page.pageParams)){//如果是“返回列表”，则从状态中获取之前的文章列表页码，并显示该页
      myData.pageParams=store.page.pageParams
    }
    axios({
      method: 'post',
      url: '/api/article/getAPageOfArticleVO',
      data: myData.pageParams
    })
      .then((response) => {
        if(response.data.success){
          if(response.data.map.articleVOs!=null){
            myData.articleVOs=response.data.map.articleVOs
            myData.pageParams=response.data.map.pageParams
          }else{
            ElMessageBox.alert("无文章！", '结果')
          }
          dialogVisible.value=false //隐藏对话框
        }else{
          ElMessageBox.alert(response.data.msg, '结果')
        }
        store.page.pageParams=null//将分页状态设置为null，表示不是从“返回列表”而来
      }).catch((error)=>{   //请求失败返回的数据
        ElMessageBox.alert("系统错误！", '结果')
        store.page.pageParams=null
      })
  }

  getAPage()    //调用函数，获取第一页数据
  function handleSizeChange(newRows) {//修改每页记录时，该方法会被调用，newRows由Pagination组件提供
    myData.pageParams.rows=newRows
    myData.pageParams.page=1
    getAPage()//改变每页记录数后，获取并显示第一页的记录
  }
  function handleCurrentChange(newPage) {//切换页码时，该方法会被调用，newPage由Pagination组件提供
    myData.pageParams.page=newPage
    getAPage()//改变当前页后，获取并显示新页的记录
  }

  //跳转至编辑文章前，将文章id和页码信息保存到状态，以便今后能够访问
  const editArticle = inject('editArticle')
  function editArticle1(articleId){
    store.articleId=articleId
    store.page.pageParams=myData.pageParams//跳转至编辑文章vue
    editArticle()
  }

  let selectArticleId = reactive
  const dialogVisible = ref(false) //是否显示对话框
  function showDialog(articleId){   
    selectArticleId=articleId
    dialogVisible.value=true //显示对话框
  }

  function deleteArticle(){   //删除文章
    axios({
      method: 'post',
      url: '/api/article/deleteById?id='+selectArticleId
    })
      .then((response) => {
        if(response.data.success){
          ElMessageBox.alert("删除成功！", '结果')
          getAPage()//删除文章后，重新获取当前页数据(刷新列表)
        }else{
          ElMessageBox.alert(response.data.msg, '结果')
        }
      }).catch((error)=>{   //请求失败返回的数据
        ElMessageBox.alert("系统错误！", '结果')
      })
  }   
</script>
<template>
  <el-row>
    <el-col :span="24"><h4 style="margin-left: 10px;">文章管理</h4></el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <!-- myData.articleVOs为表的数据集，stripe为斑马线效果，border为带边框 -->
      <el-table :data="myData.articleVOs" stripe border style="width: 100%">
        <el-table-column label="文章标题" width="360">
          <template #default="scope">
            <!-- text-decoration:none是清除超链接下划线  scope.row表示当前行数据，通过它可以访问title等属性-->
            <router-link :to="{path: '/article_comment/'+scope.row.id }" style="text-decoration:none;">
              {{ scope.row.title }}
            </router-link>  
          </template>          
        </el-table-column>
        <el-table-column prop="created" label="发布时间" width="170" />
        <el-table-column prop="hits" label="浏览量" width="70" />
        <el-table-column prop="categories" label="所属分类" width="100" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="primary" @click="editArticle1(scope.row.id)" :icon="Edit" size="small">
              编辑
            </el-button>
            <el-button type="danger" @click="showDialog(scope.row.id)" :icon="Delete" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-col>
  </el-row>
  <el-row justify="center" align="middle" style="height: 60px">
    <el-col :span="16">
      <el-pagination v-model:currentPage="myData.pageParams.page"
        v-model:page-size="myData.pageParams.rows"
        :page-sizes="[2,4,10,20]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="myData.pageParams.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-count="7"
      />
    </el-col>
  </el-row>

  <el-dialog v-model="dialogVisible" title="注意" width="30%" center>
    <span>确定要删除该文章吗？</span>
    <template #footer>
      <span class="dialog-footer">
      <el-button @click="dialogVisible=false">取 消</el-button>
      <el-button type="primary" @click="deleteArticle">确 定</el-button>  
      </span>
    </template>
  </el-dialog>
</template>
<style scoped>

</style>