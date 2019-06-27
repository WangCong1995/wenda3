package com.nowcoder.async;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件的模型
 * 不同的事件可能会有不同的字段。但事件总会有公共的东西.
 * EventModel:当时事件发生的现场
 */
public class EventModel {
    /*给您点了个赞，系统就会发一个站内信*/
    private EventType type;//事件类型（点赞）（评论）
    private int actorId;//触发者（谁点了赞）（谁评论了）
    private int entityType;//触发的载体（给哪个东西点了赞）（评论的是哪一个题目）
    private int entityId;
    private int entityOwnerId;//该条赞或评论，是属于哪个人的。方便以后发站内信，或者进行人与人之间关联
    /* 上面这些变量，都可以存到下面的exts字段里面，但这些字段很常用，为了读取方便，我们把这些字段独立出来*/


    /**
     * 扩展的字段。
     * 事件发生的那个时刻，有各种各样的信息要保留下来。为了能保留所有的信息，就要有一个扩展的字段。有点像那个ViewObject
     */
    private Map<String, String> exts = new HashMap<String, String>();

    //默认构造函数
    public EventModel(){

    }

    //自定义构造函数
    public EventModel(EventType type){
        this.type=type;
    }


    public EventModel setExt(String key, String value) {
        exts.put(key, value);
        return this;
    }

    public String getExt(String key) {
        return exts.get(key);
    }


    public EventType getType() {
        return type;
    }

    /**
     * EventModel.setType().setXX().setXX();
     * 通过这种方式，就可以通过链式调用，一口气设置全部的属性
     * @param type
     * @return
     */
    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel setExts(Map<String, String> exts) {
        this.exts = exts;
        return this;
    }


}
