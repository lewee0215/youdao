package com.liuwei.sharding.jdbc.common;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by evanliu on 2017/1/9.
 */
public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    protected String redirectTo( String url ) {
        StringBuffer rto = new StringBuffer("redirect:");
        rto.append(url);
        return rto.toString();
    }

    protected ResponseVO getSuccess(){
        return success();
    }
    
    protected ResponseVO returnSuccess(){
        return success();
    }
    
    protected ResponseVO getFromData(Object data){
        ResponseVO responseVO = getSuccess();
        responseVO.setData(data);
        return responseVO;
    };

    protected ResponseVO returnData(Object data){
        ResponseVO responseVO = success();
        responseVO.setData(data);
        return responseVO;
    }
    
    protected ResponseVO getFailure(ResponseCodeEnum codeEnum){
    	return new ResponseVO(codeEnum.getCode(),codeEnum.getMessage());
    }
    
    protected ResponseVO returnFailure(ResponseCodeEnum codeEnum){
        return failure();
    }
    
    protected ResponseVO returnFailure(){
        return failure();
    }
    
    protected ResponseVO getFailure(){
        return failure();
    }
    
    protected ResponseVO getResponse(Object data){
        ResponseVO responseVO =  success();
        responseVO.setData(data);
        return responseVO;
    }
    
    private ResponseVO success(){
        return new ResponseVO(ResponseCodeEnum.SUCCESS.getCode(),"");
    }
    private ResponseVO failure(){
        return new ResponseVO(ResponseCodeEnum.SYSTEM_ERROR.getCode(),"");
    }
}

