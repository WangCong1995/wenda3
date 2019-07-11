package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/9.
 */
@Service
public class CommentService {
    @Autowired
    private CommentDAO commentDAO;  //service层，调用dao层的服务

    @Autowired
    SensitiveService sensitiveService;  //敏感词过滤

    public List<Comment> getCommentsByEntity(int entityId, int entityType) {
        return commentDAO.selectByEntity(entityId, entityType);
    }

    public int addComment(Comment comment) {
        /*在增加评论的时候，需要把评论的内容过滤一下*/
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent())); //去掉HTML标签。本质上是对标签进行了转义，标签会像文本一样显示，但不会执行
        comment.setContent(sensitiveService.filter(comment.getContent()));  //去掉敏感词
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;//大于0，才返回comment.getId()。否则返回0
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }

    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }


    /**
     * 删除一条评论。但实际上是在数据库表中修改评论的状态
     */
    public boolean deleteComment(int commentId) {
        return commentDAO.updateStatus(commentId, 1) >0;//删除成功，返回true
    }

    /**
     * 根据id获取到一条评论
     * @param id
     * @return
     */
    public Comment getCommentById(int id){
        return commentDAO.getCommentById(id);
    }
}
