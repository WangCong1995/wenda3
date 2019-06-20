package com.nowcoder.dao;

import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface MessageDAO {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, has_read, conversation_id, created_date ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{fromId},#{toId},#{content},#{hasRead},#{conversationId},#{createdDate})"})
    int addMessage(Message message);


    /**
     * 函数功能：选出和某人的对话的所有的消息。并具有分页功能
     * @param conversationId
     * @param offset 从第offse行开始（第0行是第一条记录）
     * @param limit 选limit行
     * @return
     */
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where conversation_id=#{conversationId} order by id desc limit #{offset}, #{limit}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset, @Param("limit") int limit);



    /**
     * 函数功能：根据conversation_id进行分组，分别查出每一组对话。每一组内按时间进行降序排序（时间最新的放在最前面）。各组之间也按时间进行降序排序
     * 我们将count(id)放入Message的id属性，因为这个id属性没有用上，正好拿来给我们用来放count(id)，所以count(id) as id。
     * where  from_id=#{userId} or to_id=#{userId}选与当前用户有关的消息
     * select *,count(id) as cnt from (select * from message  where  from_id=#{userId} or to_id=#{userId} order by created_date desc) tt group by conversation_id order by created_date desc
     */
    @Select({"select ", INSERT_FIELDS, " ,count(id) as id from ( select * from ", TABLE_NAME, " where from_id=#{userId} or to_id=#{userId} order by id desc) tt group by conversation_id  order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                      @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计所有当前用户未读的信息。
     * 每一个对话与两个用户相关，仅用conversationId无法分出是消息发送者还是消息接收者
     * where has_read=0 and to_id=#{userId} conversation_id=#{conversationId}。在 conversationId这个两人的对话中，当前用户是作为消息的接收者，有多少条消息被读过（即该message的has_read=0）
     */
    @Select({"select count(id) from ", TABLE_NAME, " where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int getConversationUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);


    /**
     * 将某个会话中所有的未读消息的has_read属性，全部置为1
     */
    @Update({"update ", TABLE_NAME, " set has_read=1 where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int updateHasRead(@Param("userId") int userId, @Param("conversationId") String conversationId);

}
