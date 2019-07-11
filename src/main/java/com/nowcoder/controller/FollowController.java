package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nowcoder on 2016/7/30.
 */
@Controller
public class FollowController {
    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    /**
     * 用户关注用户 的入口
     * @param userId 被关注的用户的id
     * @return
     */
    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody   /*返回一个Json串*/
    public String followUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            /*先判断hostHolder是否为空。若为空，则去登录*/
            return WendaUtil.getJSONString(999);
        }


        /* 用户关注用户 */
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        /*将关注的事件，给发出去*/
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUser().getId()) /* 当前用户是触发者 */
                .setEntityId(userId) /* 它关注的实体是一个用户 */
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityOwnerId(userId));

        /**
         * 返回值：
         * 1.code：关注成功返回0，关注失败返回1
         * 2.msg:当前用户总共关注了多少个人
         */
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
    }

    /**
     * 取关用户的入口
     * @param userId 被取关的用户
     * @return
     */
    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUser().getId())
                .setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityOwnerId(userId));

        // 返回关注的人数
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
    }

    /**
     * 关注问题
     * @param questionId 被关注的问题
     * @return
     */
    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        /* 当前用户是否登录 */
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        /* 判断用户存不存在 */
        Question q = questionService.getById(questionId);
        if (q == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUser().getId())
                .setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION)
                .setEntityOwnerId(q.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUser().getHeadUrl());
        info.put("name", hostHolder.getUser().getName());
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));//该问题有多少个粉丝
        return WendaUtil.getJSONString(ret ? 0 : 1, info);/* info里的参数，将用来渲染问题页面 */
    }

    /**
     * 取关问题
     * @param questionId 被取关的问题
     * @return
     */
    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Question q = questionService.getById(questionId);
        if (q == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUser().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    /**
     * 当前用户的粉丝列表页面
     * @param model
     * @param userId @PathVariable("uid")路径里的变量
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        } else {
            model.addAttribute("followers", getUsersInfo(0, followerIds));//若当前用户没登录，则 localUserId==0
        }
        model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followers";
    }

    /**
     * 当前用户关注的所有人的列表页面
     * @param model 这个model将返回到前端页面
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);//分页，第一页先取10个，我关注的用户的id。到时候根据id，查出这些用户的更多信息

        if (hostHolder.getUser() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));//本地用户的id，关注对象的id的List
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));//若当前用户没登录，则 localUserId==0
        }
        model.addAttribute("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followees";
    }

    /**
     * 根据一批用户的id，取出这批用户的相关信息，然后放到ViewObject里，以便后面传到前端页面
     *
     */
    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> userInfos = new ArrayList<ViewObject>();
        for (Integer uid : userIds) {
            User user = userService.getUser(uid);
            /* 先判断一下user是否存在 */
            if (user == null) {
                continue;//如果user为空，我们就将其忽略
            }
            /*不然我们就把这个存在的user的信息，放入ViewObject中*/
            ViewObject vo = new ViewObject();
            vo.set("user", user);
            vo.set("commentCount", commentService.getUserCommentCount(uid));//获取用户评论的数量
            vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, uid));//获取用户粉丝的数量
            vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER));//获取用户关注的数量

            if (localUserId != 0) {
                /* 当前用户登录了,则判断：本地登录的用户 是不是这个遍历到的用户的关注者。若是关注了，则返回true */
                vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, uid));
            } else {
                /*localUserId == 0 说明当前用户没登录*/
                vo.set("followed", false);
            }
            userInfos.add(vo);  //把遍历到的当前的用户的数据，加到List<ViewObject>中。然后进行下一轮循环
        }
        return userInfos;   //返回List<ViewObject>
    }
}
