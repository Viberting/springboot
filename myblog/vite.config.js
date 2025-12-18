import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },

    // 只要是包含/api的请求，都会被使用反向代理进行处理:比如请求网址:/api/getHe11o，
    // 前端服务器(端口一般为:5173)会将网址转换为http://localhost:8080/getHe11o(api/被替换为空)，
    // 然后转发到后端服务器(端口号为8080)，并接收返回结果，然后将结果转发返回给前端浏览器、
    server:{//服务器
        proxy:{//反向代理
            "/api": {
                //要跨域的域名 目标地址：后端服务器网址
                target: "http://localhost:8080",
                changeOrigin: true, //是否开启跨域，是否更改源路径
                ws: true,//代理websocked
                //将请求网址中的api替换为空''
                rewrite: (path) => path.replace(/^\/api/, '')
            }
        }
    }
})
