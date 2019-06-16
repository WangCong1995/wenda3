package com.nowcoder.model;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewObject用来把数据放在一起。
 * ViewObject不是视图，是用来传递对象到verocity用的一个中间对象
 */
public class ViewObject {  //用来传递
    private Map<String, Object> objs = new HashMap<String, Object>();

    public void set(String key, Object value) {
        objs.put(key, value);
    }

    public Object get(String key) {
        return objs.get(key);
    }
}
