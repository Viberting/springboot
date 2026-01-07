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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired private MyUserDetailsService myUserDetailsService;
    @Autowired private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired private MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    @Autowired private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/images/**","/article/articleSearch","/article/getIndexData1",
                        "/article/getAPageOfArticle","/article/getIndexData",
                        "/article/getArticleAndFirstPageCommentByArticleId",
                        "/article/selectById","/comment/getAPageCommentByArticleId",
                        "/comment/insert", "/user/register", "/user/login")
                .permitAll()
                // ✅ 修复：只对管理员接口做权限拦截，删除冗余的/user/**
                .antMatchers("/user/getUserPage", "/user/selectById", "/user/getAllAuthorities",
                        "/user/adminAddUser", "/user/adminUpdateUser")
                .hasRole("admin")
                .antMatchers("/article/deleteById","/article/getAPageOfArticleVO",
                        "/article/upload","/article/publishArticle").hasRole("admin")
                .antMatchers("/comment/getCommentPage", "/comment/searchComments").hasRole("admin")
                .antMatchers("/user/profile").authenticated()
                .anyRequest().authenticated()
                .and().formLogin().successHandler(myAuthenticationSuccessHandler)
                .failureHandler(myAuthenticationFailureHandler).permitAll()
                .and().logout().logoutUrl("/logout")
                .logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,Authentication authentication) throws IOException, ServletException {
                        request.getSession().removeAttribute("user");
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(new Result(true,"登出成功")));
                    }
                }).permitAll().and().csrf().disable();
        http.headers().frameOptions().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
    @Bean @Override public AuthenticationManager authenticationManagerBean() throws Exception { return super.authenticationManagerBean(); }
}