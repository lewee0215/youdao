package com.liuwei.sharding.jdbc.common.filters;
/**
 * Description : 
 * @Author Evan Liu
 * @Date 2019年4月24日
 */
/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.alibaba.fastjson.JSON;
import com.liuwei.sharding.jdbc.utils.ApplicationUtils;
import com.liuwei.sharding.jdbc.utils.HttpServletUtils;

public class VIPHttpTraceFilter extends OncePerRequestFilter implements Ordered {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	// Not LOWEST_PRECEDENCE, but near the end, so it has a good chance of catching all
	// enriched headers, but users can add stuff after this if they want to
	private int order = Ordered.LOWEST_PRECEDENCE - 10;

	@Override
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	//https://www.jianshu.com/p/afdd31bfbf94
	//request 的inputStream和response 的outputStream默认情况下是只能读一次， 不可重复读
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		try {
			HandlerExecutionChain chain = getHandler(request);
			if(chain!=null) {
				Object handler = chain.getHandler();
				//判断Handler为方法处理形式
				if(handler!=null && handler instanceof HandlerMethod) {
					Method method = ((HandlerMethod) handler).getMethod();
					logger.info("Method:{};URI:{};",method.getDeclaringClass().getName()+"."+method.getName(),HttpServletUtils.getRequestURI(request));
				}
				
				//判断Handler为方法静态资源形式
				if(handler!=null && handler instanceof ResourceHttpRequestHandler) {
					List<Resource> res = ((ResourceHttpRequestHandler) handler).getLocations();
					logger.info("Resource:{};URI:{}",JSON.toJSONString(res),HttpServletUtils.getRequestURI(request));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//过滤静态文件请求
		if (isStaticFile(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		//请求及相应封装
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		//封装Trace请求信息
		VIPHttpTrace trace = new VIPHttpTrace();
		trace.setRequest(new VIPHttpTrace.Request(requestWrapper));
		trace.setSessionId(requestWrapper.getSession().getId());
		trace.setPrincipal(requestWrapper.getUserPrincipal());
		
		try {
	        filterChain.doFilter(requestWrapper, responseWrapper);
		}finally {
	        logger.info("VIPHttpTrace|RequestBody:{}",HttpServletUtils.getRequestBody(requestWrapper));
	        logger.info("VIPHttpTrace|ResponseBody:{}",HttpServletUtils.getResponseBody(responseWrapper));
	        responseWrapper.copyBodyToResponse();
	        
			trace.setTimeTaken(System.currentTimeMillis() - trace.getTimestamp().toEpochMilli());
			trace.setResponse(new VIPHttpTrace.Response(responseWrapper));
			logger.info("VIPHttpTrace|url:{},{}",HttpServletUtils.getFullPath(requestWrapper),JSON.toJSONString(trace));
		}
	}
	
	public boolean isStaticFile(HttpServletRequest request) {
        ResourceUrlProvider resourceUrlProvider = (ResourceUrlProvider) ApplicationUtils.getBean(ResourceUrlProvider.class);
        String staticUri = resourceUrlProvider.getForLookupPath(request.getRequestURI());
        return staticUri != null;
    }
	
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		HandlerMapping handlerMapping = ApplicationUtils.getApplicationContext().getBean(HandlerMapping.class);
		List<HandlerMapping> handlerMappings = Collections.singletonList(handlerMapping);
		
		if (handlerMappings != null) {
			for (HandlerMapping hm : handlerMappings) {
				HandlerExecutionChain handler = hm.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}


}


