import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: () => import("../views/index.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: () => import("../views/common/Login.vue"),
    },
    {
      path: "/register",
      name: "register",
      component: () => import("../views/common/Register.vue"),
    },
    {
      path: "/admin_Main",
      name: "adminMain",
      component: () => import("../views/admin/AdminMain.vue"),
      children: [//子路由：表示publish_article,edit_article等将会在AdminMain.vue内的<RouterView />的位置显示
        {
          //当 /admin_Main/publish_article匹配成功
          //publish_article将杯渲染到 admin_Main的<router-view>内部
          path: "publish_article",
          name: "publishArticle",
          component: () => import("../views/admin/PublishArticle.vue")
        },
        {
          //当 /admin_Main/edit_article匹配成功
          //edit_article将杯渲染到admin_Main的<router-view>内部
          path: "edit_article",
          name: "editArticle",
          component: () => import("../views/admin/EditArticle.vue")
        },
        {
          path: "manage_article",
          name: "manageArticle",
          component: () => import("../views/admin/ManageArticle.vue")
        },
        // 添加评论管理路由
        {
          path: "comments",
          name: "commentManagement",
          component: () => import("../views/admin/CommentManagement.vue")
        },
        {
          path: "publish_comment",
          name: "publishComment",
          component: () => import("../views/admin/PublishComment.vue")
        }
      ]
    },
    {
      path: '/article_comment/:articleId',//:articleId表示动态路由参数
      name: 'articleAndComment',
      component: () => import('../views/ArticleAndComment.vue')
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/Search.vue')
    }
  ]
});
export default router;