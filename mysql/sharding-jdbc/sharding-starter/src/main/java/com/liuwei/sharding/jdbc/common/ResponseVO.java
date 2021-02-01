package com.liuwei.sharding.jdbc.common;
import java.io.Serializable;

/**
 * restful统一返回对象
 * Created by evanliu on 2017/1/24.
 */
public class ResponseVO implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
     * 成功标识
     */
    private boolean success;


    /**
     * 错误信息
     */
    private String message;

    /**
     * 错误码
     */
    private int code;


    /**
     * 返回对象
     */
    private Object data;

    /**
     * 提示类型，，此处主要是用于前端统一处理提示标识
     */
    private String reminder;

    /**
     * 错误级别，，
     */
    private int errorLevel;


    public ResponseVO(){}


    public ResponseVO(int code,String message){
        this.message = message;
        this.code = code;
        if(code>=20000 && code < 30001){
            this.success = true;
        }
    };

    public ResponseVO(boolean success,String message,int code){
        this.message = message;
        this.code = code;
        this.success = success;
    };

    public ResponseVO(String message,int code,Object data){
        this(code,message);
        this.data = data;
    };

    public ResponseVO(boolean success,String message,int code,Object data){
        this(success,message,code);
        this.data = data;
    };
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }
}
