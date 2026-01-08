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
                // 新增：启用CORS（否则跨域配置不生效）
                .cors().and()
                .authorizeRequests()
                // 1、静态资源 + 公开接口（无需登录）
                .antMatchers(
                        "/images/**",          // 图片静态资源
                        "/article/articleSearch",
                        "/article/getIndexData1",
                        "/article/getAPageOfArticle",
                        "/article/getIndexData",
                        "/article/getArticleAndFirstPageCommentByArticleId",
                        "/article/selectById",
                        "/comment/getAPageCommentByArticleId",
                        "/comment/insert",
                        "/user/register",      // 注册
                        "/user/login"          // 登录
                ).permitAll()// 任意访问（无需登录）

                // 2、普通用户 + 管理员都可访问的接口（核心修复：添加 /api 前缀 + 调整顺序）
                .antMatchers(
                        "/user/uploadImage",                       //开放上传权限
                        "/user/profile",                          // 用户个人信息（GET/PUT）
                        "/user/**/articles",                      // 用户文章列表（GET）
                        "/user/**/stats",                           //统计
                        "/article/upload",                        // 图片上传（POST）
                        "/api/article/publishArticle",               // 发布文章
                        "/api/article/deleteById",
                        "/api/article/getAPageOfArticleVO",
                        "/api/user/updateProfile"
                ).hasAnyRole("common", "admin")

                // 3、仅管理员可访问的用户管理接口
                .antMatchers(
                        "/api/user/getUserPage",
                        "/api/user/selectById",
                        "/api/user/getAllAuthorities"
                ).hasRole("admin")

                // 4、其他 /api/user/** 接口（兜底，仅管理员）
                .antMatchers("/api/user/**").hasRole("admin")

                // 5、所有其他请求都需要登录认证
                .anyRequest().authenticated()

                .and()
                // 2、自定义用户登录控制
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