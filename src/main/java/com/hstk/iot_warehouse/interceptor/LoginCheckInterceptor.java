package com.hstk.iot_warehouse.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override // 目标资源方法运行前运行，返回 true: 放行, 返回 false: 不放行
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        // 1. 获取请求 URL
        String url = req.getRequestURL().toString();
        //log.info("Requested URL: {}", url);
        
        // 放行 OPTIONS 请求 (CORS 预检)
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            return true;
        }

        // 2. 获取请求头中的令牌 (Token)
        String authHeader = req.getHeader("Authorization");

        // 3. 判断令牌是否存在
        if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.info("Request token is empty or invalid format, return NOT_LOGIN");
            // Set 401 status
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            Result<String> error = new Result<>(0, "NOT_LOGIN", null);
            String notLogin = new ObjectMapper().writeValueAsString(error);
            resp.getWriter().write(notLogin);
            return false;
        }

        // 4. extract token
        String jwt = authHeader.substring(7);

        // 5. 解析 token
        try {
            Claims claims = JwtUtils.parseJwt(jwt);
            req.setAttribute("id", claims.get("id"));
            req.setAttribute("role", claims.get("role"));
            req.setAttribute("username", claims.get("username"));
        } catch (Exception e) {
            log.error("Token parse failed: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Result<String> error = new Result<>(0, "NOT_LOGIN", null);
            resp.getWriter().write(new ObjectMapper().writeValueAsString(error));
            return false;
        }

        // 6. 放行
        //log.info("Token is valid, allowing access...");
        return true;
    }

    @Override // 目标资源方法运行后运行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println("postHandle...");
    }

    @Override // 视图渲染完毕后运行，最后运行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println("afterCompletion...");
    }
}
