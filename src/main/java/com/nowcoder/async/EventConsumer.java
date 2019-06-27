package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler和Event之间的关系，是通过Consumer来维护的。
 * EventConsumer是第一手知道从队列中取出的是哪个类型的Events，
 * 它需要将Events分发到不同的Handler里面去，
 * 所以需要它把所有handler和Event之间的关系建立起来。
 *
 * EventConsumer在初始化的时候。通过容器里面所有实现EventHandler的接口的类，来自动化注册的。
 * 以后如果新写了一个实现了Handler接口的类。在consumer里面，在初始化的时候，就能把它发现，并注册好。
 */
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{
    public static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);


    /**
     * 下次Event进来的时候，就可以根据EventType，找出与这个EventType关联的List<EventHandler>，然后就可以一个一个的执行这些Handler了
     * 【注意：我们在下面的afterPropertiesSet()方法中，已经将EventType和Handler关联到config中了，下次再由event进来的时候，就能找到这个event对应的handler】
     */
    private Map<EventType,List<EventHandler>> config=new HashMap<EventType,List<EventHandler>>();

    //上下文
    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    /**
     * 有多少handler不可能去手动配置。
     * 我们通过接口的方式。通过Spring框架，直接到Spring的上下文里面去找，有哪几个类的定义是实现了刚刚那个EventHandler
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化，把config变量给初始化好.
        //我也不知道我现在这个工程里面，有多少个EventHandler接口的实现类。
        //1.系统启动后，通过下面这个函数，就能把所有这些EventHandler接口的实现类找出来。
        //2.找出来这些实现类之后，再通过EventHandler接口的 List<EventType> getSupportEventTypes()方法，就能知道这个handler要处理哪些eventType
        //3.然后在config中，把eventType和Handler关联起来
        Map<String,EventHandler> beans =applicationContext.getBeansOfType(EventHandler.class);//(1)找到所有的EventHandler
        /*总结：
           （1）EventConsumer在初始化的时候。通过容器里面所有实现EventHandler的接口的类，来自动化注册的。
           （2）以后如果新写了一个实现了EventHandler接口的类。在EventConsumer里面，在初始化的时候，就能把它发现，并注册好。
           （3）这样对扩展性很方便。以后要加Handler直接一加就可以了，不用去手动配置。
        */


        if(beans!=null){
            //把所有这些handler都注册到 config里面去。（直接从上下文容器里找，这样就不用通过配置文件去登记了，只有你写好了接口的实现类，系统在初始化的时候，自己就会注册好）
            for(Map.Entry<String,EventHandler> entry : beans.entrySet()){
                List<EventType> eventType=entry.getValue().getSupportEventTypes();//（2）找出这个Handler(即entry.getValue())关注哪些EventType
                //（3）将这个Handler与其所有感兴趣的EventType关联起来，注册在了config中
                for (EventType type:eventType){     //遍历当前这个handler关心的 所有EventType
                    if (!config.containsKey(type)){  //如果这个EventType第一次出现
                        //如果不包含这个key，即第一次去注册这个事件
                        config.put(type,new ArrayList<EventHandler>());

                    }
                    config.get(type).add(entry.getValue());//在当前的EventType中，加上当前的Handler。（即将这个EventType与这个Handler关联起来，注册在了config中）

                }
            }

        }

        /* 起一个线程，一直去取队列里面的事件 */
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){ //死循环
                    //先把队列的key找到
                    String key= RedisKeyUtil.getEventQueueKey();
                    //从Redis中的队列里面取出event。从队列的从最后一个元素（即队列右侧）取，先进先出
                    List<String> events=jedisAdapter.brpop(0,key);//这个0表示：如果队列中没有event时，这个就一直卡着
                    //取到event之后，就要找出这个event关联的handler去处理
                    for (String message:events){
                        if(message.equals(key)){ //过滤掉events的第一个参数key。这是jedisAdapter.brpop(0,key);的返回的第一个值就是key，所有我们要把这个key过滤掉
                            continue;
                        }

                        //将从Redis中取出的Jsan串，反序列化成EventModel对象
                        EventModel eventModel= JSON.parseObject(message,EventModel.class);

                        if(!config.containsKey(eventModel.getType())){  //若config中，没有注册对这个EventType的Handler
                            //如果不处理，说明这是一个非法的事件
                            logger.error("不能识别的事件");
                            continue;
                        }

                        for(EventHandler handler:config.get(eventModel.getType())){ //找到 所有的与这个EventType关联的Handler，然后一个一个的处理掉
                            handler.doHandle(eventModel);//把当前handler处理掉
                        }
                    }
                }
            }
        });
        thread.start(); //启动线程

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;//把这个ApplicationContext接口存储下来

    }
}
