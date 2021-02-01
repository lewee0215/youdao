package com.liuwei.sharding.jdbc.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;


public class HttpServletUtils {
	
	public static void main(String[] args) {
		URI uri = URI.create("Http://www.baidu.com?name=liuwei");
		System.out.println(getFullPath(uri));
	}
	
    public static HttpServletRequest getHttpServletRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }
    
    /**
     * 判断是否为文件上传请求
     * @param request
     * @return
     */
	public static boolean isMultipart(HttpServletRequest request) {
		//if(request instanceof MultipartHttpServletRequest)
		StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
		return resolver.isMultipart(request);
	}
    
	/**
	 * 判断是否为静态资源请求
	 * @param request
	 * @return
	 */
	public static boolean isStaticFile(HttpServletRequest request) {
        ResourceUrlProvider resourceUrlProvider = (ResourceUrlProvider) ApplicationUtils.getBean(ResourceUrlProvider.class);
        String staticUri = resourceUrlProvider.getForLookupPath(request.getRequestURI());
        return staticUri != null;
    }
	
	/**
	 * 获取请求的完整URL路径
	 * 原文链接：https://blog.csdn.net/weixin_38759449/article/details/82954026
	 * 示例： http://localhost:8080/DemoName/AServlet?username=xxx&password=yyy
	 * @param request
	 * @return
	 */
	public static String getFullPath(HttpServletRequest request) {
		StringBuffer urlBuffer = new StringBuffer();
		urlBuffer.append(StringUtils.removeEnd(request.getRequestURL().toString(), "?"));
		if (StringUtils.isNotEmpty(request.getQueryString())) {
			urlBuffer.append("?");
			urlBuffer.append(request.getQueryString());
		}
		return urlBuffer.toString();
	}
	
	/**
	 * 获取URI的完整URL路径
	 * @param uri
	 * @return
	 */
	public static String getFullPath(URI uri) {
		return uri.toString();
//		StringBuffer urlBuffer = new StringBuffer();
//		urlBuffer.append(StringUtils.removeEnd(uri.getRawPath(), "?"));
//		if (StringUtils.isNotEmpty(uri.getRawQuery())) {
//			urlBuffer.append("?");
//			urlBuffer.append(uri.getRawQuery());
//		}
//		return urlBuffer.toString();
	}
	
	public static String getScheme(HttpServletRequest request) {
		return request.getScheme();//获取协议，http
	}
	
	public static String getServerName(HttpServletRequest request) {
		return request.getServerName();//获取服务器名，localhost
	}
	
	public static int getServerPort(HttpServletRequest request) {
		return request.getServerPort();//获取服务器端口，8080
	}
	
	public static String getContextPath(HttpServletRequest request) {
		return request.getContextPath();//获取项目名，/DemoName
	}
    
	public static String getServletPath(HttpServletRequest request) {
		return request.getServletPath();//获取Servlet路径，/AServlet
	}
    
	public static String getQueryString(HttpServletRequest request) {
		return request.getQueryString();//获取参数部分，即问号后面的部分，username=xxx&password=yyy
	}
    
	public static String getRequestURI(HttpServletRequest request) {
		return request.getRequestURI();//获取请求URI，即项目名+Servlet路径，/DemoName/AServlet
	}
    
	public static String getRequestURL(HttpServletRequest request) {
		return request.getRequestURL().toString();//获取请求URL，等于不包括参数的整个路径，http://localhost:8080/DemoName/AServlet
	}
	
	public static Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			headers.put(name, toList(request.getHeaders(name)));
		}
		return headers;
	}
	
	public static Map<String, List<String>> getResponseHeaders(HttpServletResponse response) {
		Map<String, List<String>> headers = new LinkedHashMap<>();
		for (String name : response.getHeaderNames()) {
			headers.put(name, new ArrayList<>(response.getHeaders(name)));
		}
		return headers;
	}
	
	private static List<String> toList(Enumeration<String> enumeration) {
		List<String> list = new ArrayList<>();
		while (enumeration.hasMoreElements()) {
			list.add(enumeration.nextElement());
		}
		return list;
	}
	
    /**
     * 打印请求参数
     * @param request
     */
	public static String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if(wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if(buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                return payload.replaceAll("\\n","");
            }
        }
        return "";
    }

    /**
     * 打印请求参数
     * @param response
     */
	public static String getResponseBody(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if(wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if(buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = "[unknown]";
                }
                return payload;
            }
        }
        return "";
    }




}
