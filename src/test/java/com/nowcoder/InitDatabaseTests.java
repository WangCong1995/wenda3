package com.nowcoder;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WendaApplication.class)
@Sql("/init-schema.sql")  //每次跑测试用例的时候，会执行这个脚本。这个脚本放在 resource文件夹下的 init-schema.sql脚本
public class InitDatabaseTests {
    @Autowired
    UserDAO userDAO;//直接引用UserDAO对象。就像之前我们引用Service一样，依赖注入 直接拿过来用

    @Autowired
    QuestionDAO questionDAO;//依赖注入一个QuestionDAO对象，不用自己去new了

    @Test
    public void contextLoads() {
        Random random = new Random();
        for (int i = 0; i < 11; ++i) {
            User user = new User();
            user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));//随机生成一个头像
            user.setName(String.format("USER%d", i));//设置用户名
            user.setPassword("");
            user.setSalt("");
            userDAO.addUser(user);

            user.setPassword("newpassword");
            userDAO.updatePassword(user);//更新了密码

            Question question = new Question();
            question.setCommentCount(i);
            Date date = new Date();
            date.setTime(date.getTime() + 1000 * 3600 * 5 * i);//在这里故意把创建时间错开一点因为后面还要排序
            question.setCreatedDate(date);
            question.setUserId(i + 1);
            question.setTitle(String.format("TITLE{%d}", i));
            question.setContent(String.format("Balaababalalalal Content %d", i));
            questionDAO.addQuestion(question);
        }

        Assert.assertEquals("newpassword", userDAO.selectById(1).getPassword());//id为1的用户的密码是否等于“newpassword”
        userDAO.deleteById(1);
        Assert.assertNull(userDAO.selectById(1));//id为1的用户是否存在

        System.out.print(questionDAO.selectLatestQuestions(0,0,10));
    }
}
