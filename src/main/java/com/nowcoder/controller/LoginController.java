package com.nowcoder.controller;

import com.nowcoder.model.User;
import com.nowcoder.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    @Autowired
    UserService userService;


    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})    //注册请求一般是POST，它是写入数据的
    public String reg(Model model,
                      @RequestParam("username") String username, //请求参数，参考后面这个路径里的type什么的。http://127.0.0.1:8080/profile/rex/123?type=2&key=qqq
                      @RequestParam("password") String password,
                      @RequestParam(value = "next",required = false) String next,
                      HttpServletResponse response) {

        try {
            //map什么都没有，说明注册条件都通过了。如果前面带有“msg”说明肯定出问题了

            Map<String, String> map = userService.register(username, password);

            if (map.containsKey("ticket")) {
                Cookie cookie=new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie); //第一次注册，直接就下发一个cookie

                if(StringUtils.isNotBlank(next)){  //发现有一个应该跳过去的页面，就不跳转到首页了
                    return "redirect:"+next;
                }

                return "redirect:/";//注册成功后自动登录，调回首页

            }else{
                model.addAttribute("msg", map.get("msg"));//出了问题，要将问题返回给前端
                return "login";//出问题的话，返回到login的页面，让他重新再去登录
            }


        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());//出异常，先记录在日志上
            return "login"; //出现异常，要重新转回到登录页面
        }

    }

    @RequestMapping(path = {"/login/"}, method = {RequestMethod.POST})    //注册请求一般是POST，它是写入数据的
    public String login(Model model,
                        @RequestParam("username") String username,  //从前端页面传过来的请求参数。
                        @RequestParam("password") String password,
                        @RequestParam(value = "next",required = false) String next, //把埋在 login.html的form里的next也提交过来
                        @RequestParam(value = "rememberme",defaultValue = "false") boolean rememberme,  //默认值为false
                        HttpServletResponse response) {   //从服务器下发ticket到浏览器，需要通过HttpServletResponse,把这个response写入cookie中

         try {
            //map什么都没有，说明登录条件都通过了。如果前面带有“msg”说明肯定出问题了
            Map<String, String> map = userService.login(username, password);//【第一步】去验证用户名和密码

            if (map.containsKey("ticket")) { //如果map中包含有“ticket”这样的key，说明登录成功
                //【第三步】将这个ticker下发给客户端
                Cookie cookie=new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                response.addCookie(cookie); //【第四步】需要把这个cookie放到我们的response里面

                if(StringUtils.isNotBlank(next)){  //登录的事情做完以后，发现有个待跳转的地址，于是就跳转到next所代表的页面。就不跳转到首页了
                    return "redirect:"+next;
                }

                return "redirect:/";//【第五步】能运行到这里说明登录成功，所以回到首页去

            }else{
                model.addAttribute("msg", map.get("msg"));//出了问题，要将问题返回给前端
                return "login";//出问题的话，返回到login的页面，让他重新再去登录
            }


        } catch (Exception e) {
            logger.error("登录异常" + e.getMessage());//出异常，先记录在日志上
            return "login"; //出现异常，要重新转回到登录页面
        }

    }

    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})    //注意这里是GET方法。POST方法的页面，不能直接输入地址访问
    public String reg(Model model,@RequestParam(value = "next",required = false) String next) {
        model.addAttribute("next",next);    //把传过来的next参数 ，先传到前端页面中。然后将这个next埋在 login.html中的 form里面
        return "login";//跳到 login.html 页面
    }

    /*登出*/
    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET})
    public String logout(@CookieValue("ticket") String ticket) {  /*直接从cookie里面读取ticket*/
        userService.logout(ticket);     /*修改ticket的状态*/
        return "redirect:/";    /*登出之后,返回首页*/
    }
}
