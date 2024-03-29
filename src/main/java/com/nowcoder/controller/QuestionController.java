package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;  //可以从中直接取到当前登录的用户

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;



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


    @RequestMapping(value = "/question/{qid}", method = {RequestMethod.GET})
    public String questionDetail(Model model, @PathVariable("qid") int qid) {
        Question question=questionService.selectById(qid);
        model.addAttribute("question",question);
        //model.addAttribute("user",question.getUserId());//还要把用户加上，问题是和用户相关的。

        //与这个question相关的评论的信息，也需要在这里传到前端页面
        //不仅需要评论的信息，还需要评论人的用户名、用户头像等 用户信息
        //ViewObject 包含里面所有的东西，将所需的信息整合起来
        List<Comment> commentList=commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);//查出这个问题下，所有的评论
        List<ViewObject> comments=new ArrayList<ViewObject>();
        for (Comment comment:commentList){
            ViewObject vo=new ViewObject();
            vo.set("comment",comment);

            /*判断当前用户是否点赞了这个评论*/
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));    //用户信息
            comments.add(vo);
        }
        model.addAttribute("comments",comments);    //将整合的与评论相关的信息，传到前端页面

        return "detail";    //跳转到 detail.html
    }

}
