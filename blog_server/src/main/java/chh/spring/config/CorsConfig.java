package chh.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类（解决前端跨域请求Cookie无法传递的问题）
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置所有接口允许跨域
        registry.addMapping("/**")
                // 允许前端的源（你的前端运行地址，注意不要带斜杠）
                .allowedOrigins("http://localhost:5173")
                // 允许的请求方法（GET/POST/PUT/DELETE等）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头
                .allowedHeaders("*")
                // 关键：允许携带Cookie（登录态）
                .allowCredentials(true)
                // 预检请求缓存时间（减少OPTIONS请求次数）
                .maxAge(3600);
    }
}