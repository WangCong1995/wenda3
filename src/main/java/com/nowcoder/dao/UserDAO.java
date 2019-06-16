package com.nowcoder.dao;

import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;

/**
 * Created by nowcoder on 2016/7/2.
 */
@Mapper
public interface UserDAO {
    // 注意空格
    String TABLE_NAME = " user ";//user前后都有一个空格，免得和其他关键字连起来了。TABLE_NAME表示 表名。这样写的原因是：表名可能被修改，这样写能提高可维护性。
    String INSERT_FIELDS = " name, password, salt, head_url ";//INSERT_FIELDS，把insert的字段，也都独立出来。这些列名的写法要和数据库一致
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{name},#{password},#{salt},#{headUrl})"})
    int addUser(User user);//#{name}可以从 user对象中读取出name属性的值

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})  //#{id} 等于 传入方法的参数(int id)
    User selectById(int id);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where name=#{name}"})  //#{id} 等于 传入方法的参数(int id)
    User selectByName(String name);

    @Update({"update ", TABLE_NAME, " set password=#{password} where id=#{id}"})
    void updatePassword(User user);

    @Delete({"delete from ", TABLE_NAME, " where id=#{id}"})
    void deleteById(int id);
}
