package com.liuwei.sharding.jdbc.config;

import com.liuwei.sharding.jdbc.common.ResponseCodeEnum;
import com.liuwei.sharding.jdbc.common.ResponseVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Created by evanliu on 2017/3/22.
 */
@Aspect
@Configuration
@ComponentScan({"com.vipabc.basic.common.util.exception", "com.vipabc.basic.common.util.Aspect"})
public class AspectConfig {
	private static final Logger logger = LoggerFactory.getLogger(AspectConfig.class);

    @Pointcut("execution(* com.liuwei.sharding.jdbc.controller.*Controller.*(..))")
    public void executeController(){}

    /**
     * 拦截器具体实现
     * @param pjp
     * @return JsonResult（被拦截方法的执行结果，或需要登录的错误提示。）
     */
    @Around("executeController()") //指定拦截器规则；也可以直接把“execution(* com.xjj.........)”写进这里
    public Object Interceptor(ProceedingJoinPoint pjp){
        Object result = null;
        try {
            result = pjp.proceed();
        }catch(IllegalArgumentException ia){
        	ia.printStackTrace();
        	logger.error(ia.getMessage());
            result = new ResponseVO(ResponseCodeEnum.PARAM_ERROR.getCode(), ia.getMessage());
        }catch(Exception t){
        	t.printStackTrace();
        	logger.error(t.getMessage());
            result = new ResponseVO(ResponseCodeEnum.SYSTEM_ERROR.getCode(), ResponseCodeEnum.SYSTEM_ERROR.getMessage()+(t.getMessage()==null?"":t.getMessage()));
        }catch(Throwable t){
        	t.printStackTrace();
        	logger.error(t.getMessage());
            result = new ResponseVO(ResponseCodeEnum.SYSTEM_ERROR.getCode(),ResponseCodeEnum.SYSTEM_ERROR.getMessage()+(t.getMessage()==null?"":t.getMessage()));
        }
        return result;
    }


    @Bean("messageSource")
    public ReloadableResourceBundleMessageSource messageSource(){
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(1);
        messageSource.setBasename("classpath:/i18n/message");
        return messageSource;
    }

}
