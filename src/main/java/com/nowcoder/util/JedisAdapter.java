package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.controller.CommentController;
import com.nowcoder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.List;

/**
 * 通过JedisAdapter来对Jedis的操作来做一些包装
 */
@Service
public class JedisAdapter implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);

    private JedisPool pool;  //在afterPropertiesSet()里面初始化

    public static void print(int index, Object obj) {
        System.out.println(String.format("%d, %s", index, obj.toString()));
    }

    /**
     * 运行这些测试用例之前，需要打开redis数据库的服务端
     * @param args
     */
    public static void main(String[] args) {
        Jedis jedis=new Jedis("redis://localhost:6379/9");    //连接6379端口的“9”数据库。若构造函数参数为空，则默认连接本机的6379端口。

        jedis.flushDB();//把这个数据库删掉。flushAll()把所有的数据库都删掉

        //get & set
        jedis.set("hello","world");//往数据库9中的key“hello”里面，存入值“world”
        print(1,jedis.get("hello"));
        jedis.rename("hello", "newhello");
        print(1, jedis.get("newhello"));    //修改键名
        jedis.setex("hello2", 15, "world");//设置超时时间，这个“world”只存15秒。好处是：这个数据不需要自己去删除，数据库在时间到了以后数据库会去帮你删除。“短信验证码”常用这个功能去做，如果超过了指定时间，那验证码就对不上了，这时需要再请求一个验证码
        //可以把一个对象，转换成一段文本 ，然后把这个文本存入数据库。然后在数据库中，可以查到这个文本，然后将其反序列化成一个对象。

        //数值的操作
        jedis.set("pv", "100");
        jedis.incr("pv");   //数值加1
        jedis.incrBy("pv", 5);  //数值加5
        print(2, jedis.get("pv"));
        jedis.decrBy("pv", 2);  //数值减2
        print(2, jedis.get("pv"));

        print(3, jedis.keys("*"));  //打印出数据库中的所有key。参数是一个正则表达式。相当于redis命令行里面的 "keys *" 指令

        /*下面是redis真正好用的地方*/
        // list，常用来保存最近的几个
        String listName = "list";
        jedis.del(listName);
        for (int i = 0; i < 10; ++i) {
            jedis.lpush(listName, "a" + String.valueOf(i));//插入数据到list。[a9, a8, a7, a6, a5, a4, a3, a2, a1, a0]
        }
        /* 注意：对 List的操作都是以“l”开头 */
        print(4, jedis.lrange(listName, 0, 12));//取list中的元素。从0开始，取到12
        print(4, jedis.lrange(listName, 0, 3));//[a9, a8, a7, a6]  第0个元素是a9。最新插入的，放在list的最前面
        print(5, jedis.llen(listName)); //list的长度。此时长度为10
        print(6, jedis.lpop(listName)); //弹出
        print(7, jedis.llen(listName)); //经过上面的弹出操作后，a9被删除了，长度为9
        print(8, jedis.lrange(listName, 2, 6));//从[a8, a7, a6, a5, a4, a3, a2, a1, a0]取出了[a6, a5, a4, a3, a2]
        print(9, jedis.lindex(listName, 3));//查找下标为3的元素
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER, "a4", "xx"));   //在a4的后面插入xx
        print(10, jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE, "a4", "bb"));  //在a4的前面插入bb
        print(11, jedis.lrange(listName, 0 ,12));

        //hash。如果对象的"属性"随时地增加和删除比较频繁，那么用hash是比较合适的
        String userKey = "userxx";
        jedis.hset(userKey, "name", "jim");//给这个user设置name属性
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "18618181818");
        print(12, jedis.hget(userKey, "name"));
        print(13, jedis.hgetAll(userKey));
        jedis.hdel(userKey, "phone");//删除属性"phone"
        print(14, jedis.hgetAll(userKey));
        print(15, jedis.hexists(userKey, "email"));//是否存在这样一个key
        print(16, jedis.hexists(userKey, "age"));
        print(17, jedis.hkeys(userKey));//显示该对象的所有key
        print(18, jedis.hvals(userKey));//显示该对象的所有value
        jedis.hsetnx(userKey, "school", "zju");//设置额外的字段。nx表示not exists。
        jedis.hsetnx(userKey, "name", "yxy");//hsetnx()这个函数，不存在的字段才会设置进去。如果这个字段已经存在，那么不进行修改
        print(19, jedis.hgetAll(userKey));


        //set 有插入和删除操作的话，集合的时间复杂度会比list低很多。
        //集合有 交叉并补等操作。
        //集合的天生属性就是：“去重”
        String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i = 0; i < 10; ++i) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i*i));
        }
        print(20, jedis.smembers(likeKey1));//打印所有
        print(21, jedis.smembers(likeKey2));
        print(22, jedis.sunion(likeKey1, likeKey2));//并集
        print(23, jedis.sdiff(likeKey1, likeKey2));//差集，（likeKey1中有的，而且likeKey2中没有的）
        print(24, jedis.sinter(likeKey1, likeKey2));//交集，（两个集合中都有的）
        print(25, jedis.sismember(likeKey1, "12"));//集合“likeKey1”中，有没有“12”这个成员
        print(26, jedis.sismember(likeKey2, "16"));
        jedis.srem(likeKey1, "5");//把“5”从集合中给删掉，remove
        print(27, jedis.smembers(likeKey1));
        jedis.smove(likeKey2, likeKey1, "25");//把从前面的集合，移动到后面的集合中
        print(28, jedis.smembers(likeKey1));
        print(29, jedis.scard(likeKey1));//统计 集合中属性的个数
        print(29, jedis.srandmember(likeKey1));//从集合中，随机取一个元素出来
        print(29, jedis.srandmember(likeKey1,2));//从集合中，随机取2个元素出来


        //优先队列（在Redis里面称为：依据分值排序的集合 Sorted Sets）。
        // 优先队列有优先级的概念。优先队列是根据score来的
        //适用于排序Sorted Set
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "jim");//插入的是整形数，实际上存的是浮点数
        jedis.zadd(rankKey, 60, "Ben");
        jedis.zadd(rankKey, 90, "Lee");
        jedis.zadd(rankKey, 75, "Lucy");
        jedis.zadd(rankKey, 80, "Mei");
        print(30, jedis.zcard(rankKey));
        print(31, jedis.zcount(rankKey, 61, 100));//统计61分到100分的人数
        print(32, jedis.zscore(rankKey, "Lucy"));//查一下lucy有多少分。75.0
        jedis.zincrby(rankKey, 2, "Lucy");//给lucy加2分
        print(33, jedis.zscore(rankKey, "Lucy"));//查一下lucy有多少分
        jedis.zincrby(rankKey, 2, "Luc");//当对一个不存在的key加2分的时候。默认为对0分加2，最终得到2分。而且会将这个原本不在集合中的key，插入集合
        print(34, jedis.zscore(rankKey, "Luc"));
        print(35, jedis.zrange(rankKey, 0, 100));//查出 分数在0~100之间的所有的key
        print(36, jedis.zrange(rankKey, 0, 10));//[Luc, jim, Ben, Lucy, Mei, Lee]。注意，新插入的Luc成为了第0个元素
        print(36, jedis.zrange(rankKey, 1, 3));//注意下标为1~3的元素[jim, Ben, Lucy]是以分值[15,60,75]从小到大排序的。
        print(36, jedis.zrevrange(rankKey, 1, 3));//反过来排序（原本是升序，现在是降序）。[jim, Ben, Lucy] [75,60,15]
        print(36, jedis.zrevrange(rankKey, 0, 10));

        //把分值在60~100分的所有用户全打印出来。
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {   // 有了优先队列，就可以取出分值范围内的人
            print(37, tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
        }

        print(38, jedis.zrank(rankKey, "Ben"));//升序排名为2。[Luc, jim, Ben, Lucy, Mei, Lee]
        print(39, jedis.zrevrank(rankKey, "Ben"));//降序排名为3。[Luc, jim, Ben, Lucy, Mei, Lee]


        //根据字母来排序
        String setKey = "zset";
        jedis.zadd(setKey, 1, "a"); //五个人的分值是一样的，都为1
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");

        print(40, jedis.zlexcount(setKey, "-", "+"));   //计数。a和e之间总共有多少个元素
        print(41, jedis.zlexcount(setKey, "(b", "[d")); //2。[c,d]
        print(42, jedis.zlexcount(setKey, "[b", "[d")); //3。[b,c,d]
        jedis.zrem(setKey, "b");//把“b”删掉
        print(43, jedis.zrange(setKey, 0, 10));
        jedis.zremrangeByLex(setKey, "(c", "+");    //按范围进行删除。把c以上的（不包含c）元素都删掉
        print(44, jedis.zrange(setKey, 0 ,2));

        //Sorted Set 集合中每个元素带有分值。适用在需要排序的地方，需要取到这个人在集合中的位置，正序，反序

        //不要去死记硬背，命令，而是要去记住，这个数据结构有什么特点，适用在什么场景

        /*
        JedisPool pool = new JedisPool("redis://localhost:6379/9");//注意，这里的参数要关联上我们想要的数据库
        for (int i = 0; i < 100; ++i) {
            Jedis j = pool.getResource();
            print(45, j.get("pv"));
            j.close();//要记得释放资源！ 不然会卡死
        }
        */

        /*使用redis做缓存*/
        User user = new User();//在内存中new一个用户对象
        user.setName("xx");
        user.setPassword("ppp");
        user.setHeadUrl("a.png");
        user.setSalt("salt");
        user.setId(1);
        print(46, JSONObject.toJSONString(user));
        //使用json的序列化，来做一个简单的缓存
        jedis.set("user1", JSONObject.toJSONString(user));//将User对象序列化为Json串，存入Redis中

        String value=jedis.get("user1");
        User user2 = JSON.parseObject(value, User.class);//将Json串，反序列化为对象
        print(47, user2);
        int k = 2;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("redis://localhost:6379/10");//初始化 JedisPool
    }


    /**
     * 对Jedis操作的包装。往Redis中增加一个键值对
     * 这里的返回值是long，与 jedis.sadd(key,value)方法底层一致
     */
    public long sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);  //返回值是long类型
        }catch (Exception e){
            logger.error("Redis操作发生异常"+e.getMessage());
        }finally {
            /*在finally中释放资源，释放资源之前要进行非空判断*/
            if (jedis!=null){
                jedis.close();
            }
        }

        return 0;
    }

    /**
     * remove 删除一个键值对
     */
    public long srem(String key,String value){
        Jedis jedis=null;
        try{
            jedis = pool.getResource();
            return jedis.srem(key, value);  //返回值是long类型
        }catch (Exception e){
            logger.error("Redis操作发生异常"+e.getMessage());
        }finally {
            /*在finally中释放资源，释放资源之前要进行非空判断*/
            if (jedis!=null){
                jedis.close();
            }
        }

        return 0;
    }

    /**
     * 求集合中元素的数量
     */
    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("Redis操作发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    /**
     * 是否存在于集合中
     */
    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);
        } catch (Exception e) {
            logger.error("Redis操作发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    /**
     * 取出队列的最后一个元素
     * @param timeout
     * @param key
     * @return
     */
    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 将键值对存入List中
     */
    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }
}
