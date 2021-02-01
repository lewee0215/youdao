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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.liuwei.sharding.jdbc.utils.HttpServletUtils;


/**
 * A trace event for handling of an HTTP request and response exchange. Can be used for
 * analyzing contextual information such as HTTP headers.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 2.0.0
 */
public final class VIPHttpTrace {

	private final Instant timestamp = Instant.now();

	private volatile Principal principal;

	private volatile Session session;

	private volatile Request request;

	private volatile Response response;

	private volatile Long timeTaken;

	public Instant getTimestamp() {
		return this.timestamp;
	}

	public void setPrincipal(java.security.Principal principal) {
		if (principal != null) {
			this.principal = new Principal(principal.getName());
		}
	}

	public Principal getPrincipal() {
		return this.principal;
	}

	public Session getSession() {
		return this.session;
	}

	public void setSessionId(String sessionId) {
		if (StringUtils.hasText(sessionId)) {
			this.session = new Session(sessionId);
		}
	}

	public Request getRequest() {
		return this.request;
	}
	
	public void setRequest(Request request) {
		this.request = request;
	}

	public Response getResponse() {
		return this.response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public Long getTimeTaken() {
		return this.timeTaken;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	/**
	 * Trace of an HTTP request.
	 */
	public static final class Request {

		private final String method;

		private final String uri;

		private final Map<String, List<String>> headers;

		private final String remoteAddress;

		public Request(HttpServletRequest request) {
			this.method = request.getMethod();
			this.uri = request.getRequestURL().toString();
			this.headers = HttpServletUtils.getRequestHeaders(request);
			this.remoteAddress = request.getRemoteAddr();
		}
		
		public Request(String method, String url,Map<String, List<String>> headers,String remoteAddress) {
			this.method = method;
			this.uri = url;
			this.headers = headers;
			this.remoteAddress = remoteAddress;
		}

		public String getMethod() {
			return this.method;
		}

		public String getUri() {
			return this.uri;
		}

		public Map<String, List<String>> getHeaders() {
			return this.headers;
		}

		public String getRemoteAddress() {
			return this.remoteAddress;
		}

	}

	/**
	 * Trace of an HTTP response.
	 */
	public static final class Response {

		private final int status;

		private final Map<String, List<String>> headers;

		public Response(HttpServletResponse response) {
			this.status = response.getStatus();
			this.headers = HttpServletUtils.getResponseHeaders(response);
		}
		
		public Response(int status, Map<String, List<String>> headers) {
			this.status = status;
			this.headers = headers;
		}

		public int getStatus() {
			return this.status;
		}

		public Map<String, List<String>> getHeaders() {
			return this.headers;
		}

	}

	/**
	 * Session associated with an HTTP request-response exchange.
	 */
	public static final class Session {

		private final String id;

		private Session(String id) {
			this.id = id;
		}

		public String getId() {
			return this.id;
		}

	}

	/**
	 * Principal associated with an HTTP request-response exchange.
	 */
	public static final class Principal {

		private final String name;

		private Principal(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

	}

}




