package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 把所有点赞和点踩的业务，包装在这里。但所有的数据，是存储在Redis里面的
 */
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 当前有多少个人喜欢该实体
     */
    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        return jedisAdapter.scard(likeKey);
    }


    /**
     * 需要找出当前用户对这个实体（评论、问题等）是点赞/点踩/既没点赞也没点踩的状态。
     * 因为下一次打开页面的时候，浏览到这个实体（评论，问题）时，会显示自己对该实体是点赞还是点踩了
     */
    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            //如果这个用户是已经点了赞的
            return 1;
        }

        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);

        //如果点了踩，则返回 -1 。如果没点赞也没点踩，则返回0。
        return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;


    }


    /**
     * 功能：某个人，"点赞"了某个实体。
     * 某个实体（评论、问题等）由entityType和entityId共同才能确定.
     * 点赞无非就是拿一个key，然后把userId放到这个key中。
     * Redis的数据库要给很多很多人用，所以key的名称是不能随便乱取的。什么样的业务，用什么样的key一定要分的很清晰
     */
    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));

        /*点了赞之后，相当于，把它从踩里面删掉了。因为不可能同时点赞和踩！*/
        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);
        jedisAdapter.srem(disLikeKey, String.valueOf(userId));

        //因为可能有很多人同时点赞，可能你点赞的时候，别人也点赞了，点赞数是5。点完赞的时候，点赞数变成8了。
        //所以要将点赞后的点赞数给返回
        return jedisAdapter.scard(likeKey);
    }

    /**
     * 点踩：某个用户点踩了某个实体
     */
    public long dislike(int userId, int entityType, int entityId) {
        String disLikeKey = RedisKeyUtil.getDislikeKey(entityType, entityId);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));

        /*点了踩之后，相当于，把它从赞里面删掉了。因为不可能同时点赞和踩！*/
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));

        //因为可能有很多人同时点赞，可能你点赞的时候，别人也点赞了，点赞数是5。点完赞的时候，点赞数变成8了。
        //所以要将点赞后的点赞数给返回
        return jedisAdapter.scard(likeKey);//注意，在点踩之后，返回的还是对该实体的喜欢的人数
    }

}
