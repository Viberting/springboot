package chh.spring.config.security;

import chh.spring.entity.dto.UserDTO;
import chh.spring.mapper.UserMapper;
import chh.spring.tools.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 当用户验证成功后，Security会自动执行其onAuthenticationSuccess方法
 */
@Component
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Override//用户验证成功后执行
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException,ServletException {
        String username = request.getParameter("username");//客户端传过来的用户名
        UserDTO userDTO = UserDTO.entityToDto(userMapper.findByName(username));
        userDTO.setAuthorities(userMapper.findAuthorityByName(username));
        request.getSession().setAttribute("user",userDTO);//将用户信息保存在session中
        Result result= new Result();
        result.getMap().put("user",userDTO);

        //返回数据给客户端，用于客户端路由跳转控制（如管理员只能访问某些功能）
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
