package com.nowcoder.async;

/**
 * 表示当前的事件是什么样的类型。
 * 枚举型。通过这个EventType来表明事件的类型。
 */
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3),    //标识这是一个什么事件
    FOLLOW(4),
    UNFOLLOW(5);

    private int value;

    /**
     * 构造函数
     */
    EventType(int value){
        this.value=value;
    }

    public int getValue(){
        return value;
    }
}
