package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每次用户登录的时候。我需要判断用户有没有异常登录，ip地址是否异常。
 * 如果登录异常的话，就发一个邮件给该用户
 */
@Component
public class LoginExceptionHandler implements EventHandler{
    @Autowired
    MailSender mailSender;//在这个Handler里面要发邮件给用户

    @Override
    public void doHandle(EventModel model) {
        /* xxx进行了一个判断，判断发现这个用户登录异常。这里其实并没有判断，只是进行了个功能演示 */
        Map<String,Object> map =new HashMap<String,Object>();//模板渲染的参数。
        map.put("username", model.getExt("username"));//从eventProducer发过来的EventModel中取出username，然后设置到map里面，之后将map传到html模板里面


        /**
         * 功能：发送邮件
         * 1.从model的Ext字段中，取出收件人的邮箱
         * 2.邮件标题
         * 3. "mails/login_exceptin.html" 待渲染的模板的路径
         * 4.map 传进模板的参数
         */
        mailSender.sendWithHTMLTemplate(model.getExt("email"),"登录IP异常","mails/login_exception.html",map);

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);//这个Handler关心登陆事件
    }
}
