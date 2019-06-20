package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Question;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;  //可以从中直接取到当前登录的用户



    /*问题的发布*/
    @RequestMapping(value="/question/add",method = {RequestMethod.POST})    //因为是写数据，所有是POST方法
    @ResponseBody   //返回的是一个我们自己生成的 JSON字符串。回要带一个code，code如果是0的话，就是一个正确的返回
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content") String content){
        try{
            Question question = new Question();  //构造一个question对象，以便通过mybatis将其存入数据库
            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);

            if(hostHolder.getUser()==null){
                //如果是没登录的状态，那就给它一个匿名用户吧
                //question.setUserId(WendaUtil.ANONYMOUS_USERID);   //匿名用户我们可以把它放到，WendaUtil里面

                //其实我们可以不用设置匿名用户，而是直接返回
                return WendaUtil.getJSONString(999);    //如果这个用户没有登录，直接让他 999 去。这里与"popupAdd.js"联系起来。返回999后，会直接跳转到登录页面

            }else{
                question.setUserId(hostHolder.getUser().getId());
            }


            //如果
            if (questionService.addQuestion(question)>0){     //如果添加函数的返回结果大于0，说明添加成功了
                return WendaUtil.getJSONString(0);      //添加问题成功，我们返回0
            }


            questionService.addQuestion(question);//controller调service，service调dao


        }catch (Exception e){
            logger.error("增加题目失败"+e.getMessage());
        }

        return  WendaUtil.getJSONString(1,"失败");

    }


    @RequestMapping(value = "/question/{qid}")
    public  String questionDetail(Model model, @PathVariable("qid") int qid){
        Question question=questionService.selectById(qid);
        model.addAttribute("question",question);
        model.addAttribute("user",question.getUserId());//还要把用户加上，问题是和用户相关的。

        return "detail.html";
    }

}
