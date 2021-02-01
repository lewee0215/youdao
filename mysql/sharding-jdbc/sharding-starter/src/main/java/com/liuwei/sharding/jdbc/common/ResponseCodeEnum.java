package com.liuwei.sharding.jdbc.common;

/**
 * API错误码枚举
 * @author evanliu
 */
public enum ResponseCodeEnum {

	SUCCESS(20000, "操作成功"),  
	
	LOGGIN_ERROR(30001, "登录失败"),  
	ILLEGAL_USER(30002, "用户名异常"),  
	ILLEGAL_PASSWORD(30003, "密码异常"),  
	ILLEGAL_TOKEN(30004, "Token 异常"),  
	UN_PERMISSION(30005, "无权限"), 

	//系统公共参数 40000-50000
	SYSTEM_ERROR(40000, "系统异常"),  
	INTERFACE_ERROR(40001, "接口异常"), 
	DATABASE_ERROR(40002, "数据库异常"),  
	IO_ERROR(40003, "IO异常"),  
	PARAM_ERROR(40004, "参数错误");
	  
	//公共业务错误码从50000-60000

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误信息
     */
    private String message;


    /**
     * 使用错误码和错误信息构造枚举
     *
     * @param code    错误码
     * @param message 错误信息
     */
    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message != null ? message : "";
    }

    /**
     * 获取错误码
     *
     * @return String
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误信息
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }
}
