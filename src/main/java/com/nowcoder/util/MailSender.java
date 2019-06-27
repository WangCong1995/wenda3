package com.nowcoder.util;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Map;
import java.util.Properties;

/**
 * Created by nowcoder on 2016/7/15. // course@nowcoder.com NKnk66
 */
@Service
public class MailSender implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MailSender.class);
    private JavaMailSenderImpl mailSender;

    @Autowired
    private VelocityEngine velocityEngine;//这个velocity的渲染引擎是spring自带的

    /**
     * 发邮件的接口
     * @param to 发给谁
     * @param subject 标题是谁
     * @param template 发邮件用哪一个HTML模板（其实就是我们在Controller里面返回的东西）
     * @param model 模板里面变量的替换用哪些。传到模板的参数都放在了model里面
     * @return
     */
    public boolean sendWithHTMLTemplate(String to, String subject,
                                        String template, Map<String, Object> model) {
        try {
            String nick = MimeUtility.encodeText("牛客中级课");//设置昵称
            InternetAddress from = new InternetAddress(nick + "<847074803@qq.com>");//发件人。注意这里的<847074803@qq.com>与下面的afterPropertiesSet()里的username一致
            MimeMessage mimeMessage = mailSender.createMimeMessage();//邮件的正文
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);//通过MimeMessageHelper来帮助我们设置正文
            String result = VelocityEngineUtils
                    .mergeTemplateIntoString(velocityEngine, template, "UTF-8", model);//（关键点）利用Velocity的渲染引擎，直接把一个模板template渲染成一个字符串。model就是传进模板的各种各样的变量。
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setSubject(subject);//设置标题
            mimeMessageHelper.setText(result, true);//设置文本。这个文本是通过Velocity的渲染引擎。参数true表示，这是html的富文本
            mailSender.send(mimeMessage);//把正文发出去
            return true;
        } catch (Exception e) {
            logger.error("发送邮件失败" + e.getMessage());
            return false;
        }
    }

    /**
     *  初始化的时候，我需要把邮件初始化
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        mailSender = new JavaMailSenderImpl();//初始化的时候，就设置一个JavaMailSender的实现
        mailSender.setUsername("847074803@qq.com");//发件人的邮箱账号
        mailSender.setPassword("izibltddztvxbchf");//注意这里要替换成qq邮箱授权码
        //mailSender.setHost("smtp.exmail.qq.com");//使用了qq邮箱的发送服务器（在qq邮箱的设置里面可以找到网址和端口号）
        mailSender.setHost("smtp.qq.com");
        mailSender.setPort(465);//设置端口
        mailSender.setProtocol("smtps");//设置协议
        mailSender.setDefaultEncoding("utf8");//设置编码
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.ssl.enable", true);//将SSL给开起来
        //javaMailProperties.put("mail.smtp.auth", true);
        //javaMailProperties.put("mail.smtp.starttls.enable", true);
        mailSender.setJavaMailProperties(javaMailProperties);
        /*运行至此，mailSender就初始化好了*/
    }
}
