package com.nowcoder.controller;

import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.SensitiveService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;


    /**
     * 入口
     * @param questionId 评论的问题的id
     * @param content 评论内容
     * @return
     */
    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})  //POST方法。因为要向数据库写入数据
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {

        try {

            Comment comment = new Comment();
            comment.setContent(content);

            if (hostHolder.getUser() != null) { //说明用户是登录的
                comment.setUserId(hostHolder.getUser().getId());
            } else {    //用户是没登录的
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);//匿名用户的id
                //return "redirect:/relogin";   //让它去登录
            }

            comment.setEntityId(questionId);
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);

            // 更新题目里的评论数量
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);
            // 如何去异步的更新？ 那个时候就不需要事务了。因为评论直接加进来就完了。评论数据的更新滞后一点也没什么。这个业务到时候我们会做成异步的
        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + String.valueOf(questionId);  //跳到评论的问题所在的页面
    }
}
