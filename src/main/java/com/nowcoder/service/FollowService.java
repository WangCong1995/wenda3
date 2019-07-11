package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 用户进行关注的时候，要做两件事情：
 * 1.将关注的对象，放到我的关注列表里面。
 * 2.将自己放到 实体的粉丝列表里面。
 */
@Service
public class FollowService {
    @Autowired
    JedisAdapter jedisAdapter;//所有的关注放在Redis里面

    /**
     * 用户关注了某个实体。可以关注问题,关注用户,关注评论等任何实体
     * @param userId 关注的发起者的id
     * @param entityType
     * @param entityId 被关注的东西的id
     * @return
     */
    public boolean follow(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);//取到 实体的粉丝列表的key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);//取到 用户的关注列表的key
        Date date = new Date();//时间。用来放在zset里面
        Jedis jedis = jedisAdapter.getJedis();//获取一个Jedis

        Transaction tx = jedisAdapter.multi(jedis);//开启一个事务
        tx.zadd(followerKey, date.getTime(), String.valueOf(userId));//将某用户id 放入 某个实体的粉丝列表
        tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId));//将某实体id 放入 某用户的关注列表
        List<Object> ret = jedisAdapter.exec(tx, jedis);//执行一个事务，并将执行结果保存到ret中

        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;//如果3个表达式都返回true。说明事务确实整体执行成功了
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean unfollow(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Date date = new Date();
        Jedis jedis = jedisAdapter.getJedis();

        Transaction tx = jedisAdapter.multi(jedis);
        tx.zrem(followerKey, String.valueOf(userId));//从 某实体的粉丝列表中，将某用户id删除。
        tx.zrem(followeeKey, String.valueOf(entityId));//从 某用户的关注列表中，将某实体id删除
        List<Object> ret = jedisAdapter.exec(tx, jedis);

        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;//取关事务成功，则返回true
    }


    /**
     * 工具函数：用来将 Set<String> 转换为 List<Integer>
     * @param idset
     * @return
     */
    private List<Integer> getIdsFromSet(Set<String> idset) {
        List<Integer> ids = new ArrayList<>();
        for (String str : idset) {
            ids.add(Integer.parseInt(str));//将集合中String 转成 Integer存入List中
        }
        return ids;
    }


    /**
     * 获取 某实体的所有的关注者的id
     * @param entityType
     * @param entityId
     * @param count score的上界：end
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int count) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);//某实体的粉丝列表，在Redis中的key
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
    }

    /**
     * 获取 某实体的所有的关注者的id，并可以分页
     * @param entityType
     * @param entityId
     * @param offset 用来分页
     * @param count
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int offset, int count) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, offset+count));
    }

    /**
     * 获取该用户的关注列表
     * @param userId
     * @param entityType
     * @param count
     * @return
     */
    public List<Integer> getFollowees(int userId, int entityType, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, 0, count));//降序排序。最新的用户排在最前面
    }

    public List<Integer> getFollowees(int userId, int entityType, int offset, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset+count));
    }

    /**
     * 某实体有多少个粉丝
     * @param entityType
     * @param entityId
     * @return
     */
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    /**
     * 某用户有多少个关注对象
     * @param userId 用户id
     * @param entityType 用户关注的对象的类型（用户或问题）。
     * @return
     */
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return jedisAdapter.zcard(followeeKey);
    }



    /**
     *  功能：判断这个用户 是否关注了 某个实体。
     *  要判断某个用户是否存在于 某实体的粉丝队列中。只需要判断：
     *  这个userId 是否在 followerKey这个队列中存在score。若存在，则说明该用户关注了这个实体。
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean isFollower(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;//不为空，则说明userId存在于 实体的粉丝队列中
    }
}
