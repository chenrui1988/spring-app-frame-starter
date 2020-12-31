package org.quick.dev.model;

public class EntityResult {

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;

    public EntityResult() {}

    public EntityResult(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    public EntityResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static EntityResult ok() {
        return new EntityResult(null);
    }

    public static EntityResult ok(Object data) {
        return new EntityResult(data);
    }

    public static EntityResult build(Integer status, String msg, Object data) {
        return new EntityResult(status, msg, data);
    }

    public static EntityResult build(Integer status, String msg) {
        return new EntityResult(status, msg, null);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
