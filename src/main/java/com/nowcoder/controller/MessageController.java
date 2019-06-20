package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model) {
        try {

            if(hostHolder.getUser()==null){ //若当前没有登录
                return "redirect:/reglogin";    //跳转到登录页面，去登录
            }

            int localUserId = hostHolder.getUser().getId(); //当前用户的id

            //取出所有与当前用户相关的对话，并按时间逆序排序
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
            List<ViewObject> conversations = new ArrayList<ViewObject>();
            for (Message message : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();    //获取到对方的id
                vo.set("user", userService.getUser(targetId));  //把与当前用户聊天的目标用户给取出来
                vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));  //未读的消息数
                conversations.add(vo);
            }
                model.addAttribute("conversations", conversations); //将所有的数据，传到页面里
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letter";
    }

    /**
     * 依据conversationId，显示出该对话中的所有的消息
     */
    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model, @Param("conversationId") String conversationId) {
        try {
            List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<ViewObject>();
            for (Message message : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                vo.set("user",userService.getUser(message.getFromId()));

                messages.add(vo);
            }
            model.addAttribute("messages", messages);
        } catch (Exception e) {
            logger.error("获取详情消息失败" + e.getMessage());
        }
        //获取消息详情成功,这时所有的未读消息已经被查看了，需要在此处，将未读的消息全部置为已读。将has_read设置为1
        messageService.updateHasRead(hostHolder.getUser().getId(),conversationId);//打开消息详情页后，将当前用户的所有未读消息置为已读

        return "letterDetail";  //跳转到letterDetail.html
    }


    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName, //消息发给谁
                             @RequestParam("content") String content) { //要发的内容
        try {
            if (hostHolder.getUser() == null) {  //hostHolder.getUser()等于null。说明用户没登录
                return WendaUtil.getJSONString(999, "未登录"); //返回一个json串，表示没登录
            }
            User user = userService.selectByName(toName);   //依据 消息接受者的名字，取出该目标用户
            if (user == null) { //如果user为空，说明目标用户不存在
                return WendaUtil.getJSONString(1, "用户不存在");
            }

            //至此，表示目标用户存在，下面开始构造消息
            Message msg = new Message();
            msg.setContent(content);
            msg.setFromId(hostHolder.getUser().getId());//从当前用户发的消息
            msg.setToId(user.getId());//发送给目标用户
            msg.setCreatedDate(new Date());
            //msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
            messageService.addMessage(msg);
            return WendaUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("增加站内信失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "插入站内信失败");
        }
    }


    @RequestMapping(path = {"/msg/jsonAddMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                             @RequestParam("toId") int toId,
                             @RequestParam("content") String content) {
        try {
            Message msg = new Message();
            msg.setContent(content);
            msg.setFromId(fromId);
            msg.setToId(toId);
            msg.setCreatedDate(new Date());
            //msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
            messageService.addMessage(msg);
            return WendaUtil.getJSONString(msg.getId());
        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "插入评论失败");
        }
    }
}
