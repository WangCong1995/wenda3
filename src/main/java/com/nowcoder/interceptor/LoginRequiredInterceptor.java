package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  将要访问的页面是强制需要登录的。如果你没登录的话，让你跳转到登录页
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if(hostHolder.getUser()==null){     //如果hostHolder.getUser()==null，说明用户没登录
            httpServletResponse.sendRedirect("/reglogin?next="+httpServletRequest.getRequestURI());      //若没登录，直接让你跳转到登录页（/reglogin），还要把当前访问的页面，作为一个参数传过去。当登录成功后，会根据这个参数，调回你之前想访问的页面
        }

        return true;//若返回false,整个请求会被终止，你会访问到一个空白的首页
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
