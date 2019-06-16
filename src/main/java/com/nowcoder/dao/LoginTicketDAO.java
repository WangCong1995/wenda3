package com.nowcoder.dao;


import com.nowcoder.model.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketDAO {
    // 注意空格
    String TABLE_NAME = " login_ticket ";//user前后都有一个空格，免得和其他关键字连起来了。TABLE_NAME表示 表名。这样写的原因是：表名可能被修改，这样写能提高可维护性。
    String INSERT_FIELDS = " user_id, expired, status, ticket ";//INSERT_FIELDS，把insert的字段，也都独立出来。这些列名的写法要和数据库一致
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ",TABLE_NAME,"(",INSERT_FIELDS,") values (#{userId},#{expired},#{status},#{ticket})"})
    int addTicket(LoginTicket ticket);//向数据库中，插入一条ticket

    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME," where ticket=#{ticket}"})  //服务器提交到数据库找的，只有ticket
    LoginTicket selectByTicket(String ticket);//查出一条ticket


    //如果登出，需要把ticket给过期掉,也就是修改ticket的状态
    @Update({"update ",TABLE_NAME," set status=#{status} where ticket=#{ticket}"})
    void updateStatus(@Param("ticket") String ticket, @Param("status") int status);

}
