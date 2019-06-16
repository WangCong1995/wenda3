package com.nowcoder.model;

import java.util.Date;

/**
 * @date 2019/6/13 16:10
 * 用这个类 来保存这个用户登录的一些信息。
 * 对应数据库表里面的 login_ticket表
 */
public class LoginTicket {
    private int id; //对应数据库中的字段
    private int userId;
    private Date expired;
    private int status;
    private String ticket;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
