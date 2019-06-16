package com.nowcoder.model;

import org.springframework.stereotype.Component;


/**
 * 拿这个对象，专门去放，我刚根据ticket取出来的用户。
 * 这样其他页面就能通过注解的方式，去引用这个HostHolder
 */
@Component
public class HostHolder {
    //访问服务器都是许多用户同时访问（多线程同时访问）。
    //private User user 如果这样写。那么两条线程同时访问的时候，这个user对象只能表示一个用户。（但是两个线程应该对应两个用户）
    //ThreadLocal 表示 线程本地的变量。这个变量每个线程都有一份拷贝（变量副本，线程本地变量）。每个线程中，该变量存放的内存是不一样的。但是它又可以通过一个公共的接口来访问
    //要让每一个登录的线程，都有自己对应的user。最合适的就是用ThreadLocal了
    private static ThreadLocal<User> users = new ThreadLocal<User>();//看上去是只有一个对象，实际上为每一条线程都分配了一个对象

    public User getUser() {
        return users.get();
    }//当去getUser()的时候，它会根据你当前的线程，去找到你当前线程保存关联的对象


    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();;
    }

}
