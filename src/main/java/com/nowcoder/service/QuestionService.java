package com.nowcoder.service;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by nowcoder on 2016/7/15.
 */
@Service
public class QuestionService {
    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    SensitiveService sensitiveService;

    public Question getById(int id) {
        return questionDAO.getById(id);
    }

    public Question selectById(int id){
        return questionDAO.selectById(id);
    }


    public int addQuestion(Question question) {
        questionDAO.addQuestion(question);//如果提交成功会返回大于零

        /* HTML标签过滤 */
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));//先把title和content中的HTML标签，在入数据库之前就给过滤掉。它只是把标签给转义了，而不是删除
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));//对title也要进行过滤

        /*敏感词过滤。利用字典树（前缀树）*/
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));

        return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
    }

    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    public int updateCommentCount(int id,int count){
        return questionDAO.updateCommentCount(id,count);
    }
}
