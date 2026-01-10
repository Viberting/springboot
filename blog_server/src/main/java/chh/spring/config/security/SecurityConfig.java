package chh.spring.config.security;

import chh.spring.tools.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 配置访问资源的权限、登入或登出时的设置
 * 合并规则：核心沿用第一段正确逻辑，保留第二段独有的接口/配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)  //启用方法级别的权限认证
public class SecurityConfig extends WebSecurityConfigurerAdapter {  //权限配置
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 保留第一段的CORS配置（第二段缺失，必须保留）
                .cors().and()
                .authorizeRequests()
                // ========== 1、静态资源 + 公开接口（无需登录） ==========
                // 基础保留第一段，补充第二段里的公开接口（无新增，两段一致）
                .antMatchers(
                        "/images/**",          // 图片静态资源
                        "/article/articleSearch",
                        "/article/getIndexData1",
                        "/article/getAPageOfArticle",
                        "/article/getIndexData",
                        "/article/getArticleAndFirstPageCommentByArticleId",
                        "/article/selectById",
                        "/comment/getAPageCommentByArticleId",
                        "/comment/getCommentTreeByArticleId",
                        "/user/register",      // 注册
                        "/user/login"          // 登录
                ).permitAll()// 任意访问（无需登录）

                // ========== 2、普通用户 + 管理员都可访问的接口 ==========
                // 核心沿用第一段，补充第二段里独有的接口：/article/publishArticle（无api前缀）、/article/deleteById（无api前缀）
                .antMatchers(
                        // 原有第一段接口
                        "/user/uploadImage",                       //开放上传权限
                        "/user/profile",                          // 用户个人信息（GET/PUT）
                        "/user/**/articles",                      // 用户文章列表（GET）
                        "/user/**/stats",                           //统计
                        "/article/upload",                        // 图片上传（POST）
                        "/api/article/publishArticle",               // 发布文章
                        "/api/article/deleteById",
                        "/api/article/getAPageOfArticleVO",
                        "/api/user/updateProfile",
                        "/user/updateProfile",
                        "/api/user/selectById",
                        "/user/selectById",
                        // 第二段独有的接口（补充进来，权限和第一段一致）
                        "/article/publishArticle",                 // 无api前缀的发布文章
                        "/article/deleteById",                     // 无api前缀的删除文章
                        "/article/getAPageOfArticleVO",             // 无api前缀的文章分页
                        "/comment/getAPageCommentByArticleId"     // 文章评论分页
                ).hasAnyRole("common", "admin")  // 保留第一段的角色名称（第二段错写USER，修正）

                // ========== 3、仅管理员可访问的接口 ==========
                // 核心沿用第一段，补充第二段里独有的管理员接口：/user/getUserPage（无api前缀）等
                .antMatchers(
                        // 原有第一段接口
                        "/api/user/getUserPage",
                        "/api/user/getAllAuthorities",
                        // 评论管理接口
                        "/admin/comment/**",
                        "/api/comment/getCommentPage",
                        "/api/comment/deleteComment",
                        "/api/comment/batchDeleteComment",
                        "/api/comment/updateStatus",
                        "/api/comment/searchComments",
                        "/api/comment/detail/**",
                        // 第二段独有的管理员接口（补充进来，权限为admin）
                        "/user/getUserPage",
                        "/user/getAllAuthorities"
                ).hasRole("admin")

                // ========== 4、兜底规则：所有其他请求都需要登录认证 ==========
                // 保留第一段的合理兜底（第二段的/user/**通配规则删除，避免覆盖）
                .anyRequest().authenticated()

                .and()
                // ========== 自定义用户登录/注销控制 ==========
                // 两段逻辑一致，完全保留
                .formLogin()
                .successHandler(myAuthenticationSuccessHandler)//权限验证成功的处理
                .failureHandler(myAuthenticationFailureHandler)//权限验证失败的处理
                .permitAll()//验证通过后可以访问任意资源

                .and()
                .logout()//注销用户
                .logoutUrl("/logout")//注销网址
                .logoutSuccessHandler(new LogoutSuccessHandler() {//注销用户成功时执行
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                        request.getSession().removeAttribute("user");
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(
                                new Result(true,"登出成功")));
                    }
                })
                .permitAll()

                .and().csrf().disable();//禁用跨站csrf攻击防御

        //防止错误：Refused to display in a frame because it set 'X-Frame-Options' to 'DENY'
        http.headers().frameOptions().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();//密码加密策略
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}