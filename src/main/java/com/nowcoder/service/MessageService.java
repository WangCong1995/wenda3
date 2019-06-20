package com.nowcoder.service;

import com.nowcoder.dao.MessageDAO;
import com.nowcoder.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;


@Service
public class MessageService {
    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent())); //去掉HTML标签
        message.setContent(sensitiveService.filter(message.getContent()));  //站内信也要敏感词过滤
        return messageDAO.addMessage(message) > 0 ? message.getId() : 0;  //将消息存入数据库
    }

    /**
     * 取出一个对话中的消息。加了分页
     */
    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConversationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }

    public int updateHasRead(int userId, String conversationId){
        return messageDAO.updateHasRead(userId, conversationId);
    }
}
