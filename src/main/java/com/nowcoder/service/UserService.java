package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private LoginTicketDAO loginTicketDAO;//依赖注入LoginTicketDAO的对象


    /**
     * 根据用户名查找用户
     */
    public User selectByName(String name) {
        return userDAO.selectByName(name);
    }


    //用户注册。返回值Map<String,String>的原因是：返回会有各种各样的属性，比如说“用户名已被注册”或者是其他的情况 就把各种字段写在这个map里。如果注册成功，则直接返回一个空，注册成功之后一般还会有一个登录的功能。
    public Map<String, String> register(String username, String password) {
        Map<String, String> map = new HashMap<String, String>();

        //StringUtils是apache提供的工具类
        if (StringUtils.isBlank(username)) {  //判断是否为空。注意：  字符串"    "   也是空
            map.put("msg", "用户名不能为空");//这些信息最后要返回到前端的页面上
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }

        //注册的用户名是不能存在的
        User user = userDAO.selectByName(username);
        if (user != null) {
            map.put("msg", "用户名已经被注册");
            return map;
        }

        // UUID 含义是通用唯一识别码 (Universally Unique Identifier).每次生成的UUID是唯一的。
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));//随机生成一个字符串，截取5个字符
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));//随机生成一个头像
        user.setPassword(WendaUtil.MD5(password + user.getSalt()));//数据库里面保存的密码是 用MD5加密的（原始密码+salt）
        userDAO.addUser(user);//将用户存入数据库

        //注册成功，也要给用户下发一个 ticket。也就是说，一注册完，自动就登录
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);

        return map;
    }

    //登录
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();

        //StringUtils是apache提供的工具类
        if (StringUtils.isBlank(username)) {  //判断是否为空。注意：  字符串"    "   也是空
            map.put("msg", "用户名不能为空");//这些信息最后要返回到前端的页面上
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }

        User user = userDAO.selectByName(username);//去查这个用户
        if (user == null) {
            map.put("msg", "用户名不存在");
            return map;
        }

        //执行到这里说明：用户名已经被查出来了
        //（用户提交上来的密码+用户的salt）的MD5，是不是与“数据库里面的存储的MD5" 一样
        if(!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            //如果MD5不一样
            map.put("msg","密码错误");
        }

        //【第二步】用户名和密码验证成功，然后后台生成一个ticket，记录这个ticket和这个用户是关联的，然后存入数据库中
        String ticket=addLoginTicket(user.getId());//将userId与ticket关联起来，并将这个ticket存入数据库中。这个ticket最终还是要下发给浏览器的
        map.put("ticket",ticket);//先把这个ticket放入map中，传到LoginController中去
        map.put("userId", user.getId());
        //怎么将ticket下发到cookie呢？通过LoginController里面的 HttpServletResponse来下发的。

        return map;     //用户名和密码验证成功，直接return一个map
    }

    //用户登录成功以后，我们需要给他下发一个ticket
    //你在注册的时候是登态，而你在登录的时候，也是登录态
    //这个函数的功能是增加一个ticket。即userId对应的这个用户要登录，这个UserService要给这个登录下发ticket
    public String addLoginTicket(int userId){
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(userId);//userId是刚刚登录的用户 或者是 刚刚注册的那个用户

        Date now=new Date(); //现在的时间
        now.setTime(1000*3600*24+now.getTime());//把当前时间往后移了1天

        loginTicket.setExpired(now);//设置一个有效期 过期的时间。

        loginTicket.setStatus(0); //有了这个过期的时间以后，再把默认的ticket状态设为0。0表示这个状态是ticket有效的。以后如果登出的话，直接把这个状态改掉就可以了

        //生成一个随机的ticket，这里还是用UUID来生成。但UUID中间是有横杠“-”的，所有要把横杠都给替换掉。
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));


        loginTicketDAO.addTicket(loginTicket);//把这个ticket存入数据库

        return loginTicket.getTicket();
    }

    //查询用户的接口
    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    /**
     * 用户登出，只需要让ticket失效
     * @param ticket
     */
    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);//让ticket失效，把它的状态改成1
    }

}
