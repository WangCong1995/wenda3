package com.nowcoder.controller;


import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.MessageService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;  //通过其获取当前用户

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;//用其来把点赞的事件发出去。需要把Event需要的数据都传过去



    /**
     * @param commentId 喜欢的某个评论
     * @return 返回一个json串到前端页面
     */
    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST}) //POST方法。因为要向数据库写入数据
    @ResponseBody  //因为要返回json串，而不是跳转到另外的页面
    public String like(@RequestParam("commentId") int commentId) {
        /*首先判断一下，用户存不存在*/
        if(hostHolder.getUser()==null){
            //若当前用户为空
            return WendaUtil.getJSONString(999);//返回999。这个码和js的代码关联
        }

        //把评论找出来，然后通过comment.getEntityId(),即可找出评论所关联的问题的id
        Comment comment=commentService.getCommentById(commentId);

        //把点赞事件发出去。其实就是创建了一个EventModel，然后序列化为Json串，存入Redis中。
        //原本点赞之后，向被点赞的用户发站内信的功能，是要在这写的。现在通过队列异步化了。
        //这里减少了代码的重复，避免了在很多的Controller里面重复写站内信的代码。通过异步化的方式，把这些公共处理的部分，都有对应的Handler来处理
        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(hostHolder.getUser().getId())
                .setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT)
                .setEntityOwnerId(comment.getUserId())
                .setExt("questionId",String.valueOf(comment.getEntityId())));


        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));//将当前点赞数，封装到json串中，发回到前端

    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})  //POST方法。因为要向数据库写入数据
    @ResponseBody  //因为要返回json串，而不是跳转到另外的页面
    public String dislike(@RequestParam("commentId") int commentId) {
        /*首先判断一下，用户存不存在*/
        if(hostHolder.getUser()==null){
            //若当前用户为空
            return WendaUtil.getJSONString(999);//返回999。这个码和js的代码关联
        }

        long likeCount = likeService.dislike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));

    }
}
