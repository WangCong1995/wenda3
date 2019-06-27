package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 某人给实体（评论、问题）点了赞，实体的拥有者会收到一个站内信.
 * 实现了EventHandler的接口，这样EventConsumer才能发现它
 */
@Component
public class LikeHandler implements EventHandler{

    //当我收到这么一个事件的时候，我就给他发一个站内信
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    /**
     * @param model 传进来一个事件
     */
    @Override
    public void doHandle(EventModel model) {
        /*构造消息*/
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);//发信人的ID。这里是系统id
        message.setToId(model.getEntityOwnerId());//收信人的ID。这个人会收到系统发过来的通知
        message.setCreatedDate(new Date());
        User user=userService.getUser(model.getActorId());//获取事件触发者的用户信息
        message.setContent("用户"+user.getName()+"赞了你的评论，http://127.0.0.1:8080/question/"+model.getExt("questionId"));//站内信内容。

        messageService.addMessage(message);//把消息发出去

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);//表示只关注like的事件
    }
}
