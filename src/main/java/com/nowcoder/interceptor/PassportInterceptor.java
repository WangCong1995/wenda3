package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

//第一个拦截器先判断一下，提交这个ticket的用户是谁，第二个拦截器再判断这个人有没有权限去访问我这个页面。
//【注意】(我们写了下面这个Interceptor,接下来需要把这个Interceptor注册进去)。简单说就是需要把这个拦截器配置到 Web Server里面
//用@Component注解后，PassportInterceptor已经是一个控制反转的对象了，bean已经存在了。可以被其他的类 来依赖注入了

//用户身份的判断
@Component  //1.要注解为Component，这样拦截器才能被依赖注入 2.要实现HandlerInterceptor接口
public class  PassportInterceptor implements HandlerInterceptor {

    //既然要做用户身份进行验证，用户提交过来cookie，所以我们要在请求之前做一个拦截。
    //把从提交过来的cookie里面把ticket取出来，看一下，它是否是一个有效的ticket。如果是一个有效的ticket，则需要把用户的信息取出来
    @Autowired
    private LoginTicketDAO loginTicketDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HostHolder hostHolder;


    //1.请求在被Controller处理之前，会先调用这个preHandle函数。如果返回false，整个请求就结束了。因为被我这个拦截器给拦住了，请求失败了.
    //这个拦截器处于所有http请求处理之前的最前面。
    //【1】这个最早的拦截器是用来判断用户身份的
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String ticket = null;
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {  //遍历这个提交过来的cookies。去找我刚下发的cookie
                if (cookie.getName().equals("ticket")) {  //cookie.getName()等于“ticket",说明我找到了这个t票
                    ticket = cookie.getValue();//找到了的ticket不一定是有效的，因为找到的ticket票可能过期了。
                    break;
                }
            }
        }

        //找到ticket之后，需要把用户的相关信息给取出来，然后将用户信息放到一个上下文。那以后所有的页面访问就知道当前这个用户信息是怎么样的
        if (ticket != null) {//不为空,说明找到这个t票了
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);//从数据库的表中，找出这个ticket
            if (loginTicket == null || loginTicket.getExpired().before(new Date()) || loginTicket.getStatus() != 0) { //判断ticket是否有效的。是否为空.是否设置的过期时间 在 当前时间之前。ticket的状态是否等于0
                //上面三个条件只要有一个为真，则此ticket是一个无效的状态。说明这个用户是没有登录的。
                return true;//那我们直接就返回。注意这里不能返回false，一旦返回false，整个请求就结束了

            }

            //如果说这个ticket是真实有效的，我们需要把这个ticket关联的用户信息给取出来，然后将用户信息放到一个上下文。其他后面接入的链路（包括Controller和service）都可以访问到这个上下文
            User user = userDAO.selectById(loginTicket.getUserId());//把用户取出来
            //【2】把这个user放入hostHolder的threadLocal变量里面，保证了后台都能直接访问到这个user
            hostHolder.setUser(user);//将取出来的这个user写入hostHolder（相当于上下文）中，这样其他地方就可以依赖注入这个hostHolder，从而读取用户的信息
            //拦截器做了这么多工作都是为了构造一个随处可用的hostHolder。可以在所有的Controller和Service里面使用

        }

        return true;//注意这里不能返回false，一旦返回false，整个请求就结束了
    }

    //2.请求处理完了以后，渲染页面之前，再执行这个函数
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && hostHolder.getUser() != null) {//想当于controller里面的model（用来将数据传到前端）。而view相当于templates文件夹里面的html页面。
            //【3】在页面渲染之前，可以把这个user直接放到verocity渲染的上下文
            modelAndView.addObject("user", hostHolder.getUser());//这样在上下文或verocity模板(即templates文件夹里面的html页面)里面就可以直接访问这个变量了

            //我为了能在所有的 渲染的 Verocity里面，都可以直接访问这个user。那我就在它的渲染之前，我就统一把这个user对象加进去
            //因为这是个拦截器，所有的controller在页面渲染之前，我都会把这个user给加进去
        }

    }

    //页面都渲染完成了，才会回调这个方法。用来清除一些数据
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        hostHolder.clear();//【4】这个请求结束的时候，把hostHolder清空。不然hostHolder里面的用户会越来越多
    }
}
