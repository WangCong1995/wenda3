package com.nowcoder.async;

import java.util.List;

/**
 * EventHandler是一个接口。它专门处理这些Event.
 * EventHandler有两个函数，第一个函数处理Event，第二个函数表明自己对哪些Event感兴趣
 */
public interface EventHandler {

    void doHandle(EventModel model);//当这个Handler关注的Event的发生的时候，就在这里处理这些Event

    /**
     * 这个接口用来注册自己.让别人知道这个Handler关注哪些Event
     * 用List的原因是Handle可能关注好几个Event
     * @return
     */
    List<EventType> getSupportEventTypes();
}
