package com.nowcoder.util;

/**
 * 专门用来生成Redis的key的工具。
 * 所有的key都从这里生成，这样就能保证所有的key不重复.
 * 为了保证不重复，无非就是加前缀，不同的业务，加不同的前缀
 */
public class RedisKeyUtil {
    private static String SPLIT=":"; //分隔符
    private static String BIZ_LIKE="LIKE";//喜欢的。BIZ表示业务
    private static String BIZ_DISLIKE="DISLIKE";//不喜欢的
    private static String BIZ_EVENTQUEUE="EVENT_QUEUE";//事件队列，异步队列



    /**
     * 生成对某个实体的唯一LikeKey，或者说 点赞的key
     * @param entityType 实体类型
     * @param entityId 实体ID
     */
    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    /**
     * 生成 dislikeKey或者说 点踩的key
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getDislikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getEventQueueKey(){
        return BIZ_EVENTQUEUE;
    }
}
