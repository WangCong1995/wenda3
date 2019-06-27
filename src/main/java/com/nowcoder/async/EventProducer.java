package com.nowcoder.async;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件的入口，由EventProducer统一的去发这些事件
 */
@Service
public class EventProducer {
    @Autowired
    JedisAdapter jedisAdapter;//引入Redis中的队列

    /**
     * 把eventModel保存到一个队列里面。
     */
    public boolean fireEvent(EventModel eventModel){
        try {
            /*把事件转换成文本，然后把兑现序列化成文本，放到redis里面。
             等到以后要用的时候，再从文本取出来，再反序列化成一个对象*/
            String json= JSONObject.toJSONString(eventModel);//先把对象转换成JSON字符串
            String key= RedisKeyUtil.getEventQueueKey();//获取队列的key
            jedisAdapter.lpush(key,json);//将JSON字符串推入 Redis的队列里面

            return true;
        } catch (Exception e) {
            return false;//如果出异常就返回false
        }

    }
}
