package com.liuwei.sharding.jdbc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class ApplicationUtils implements ApplicationContextAware{
	private final static Logger logger = LoggerFactory.getLogger(ApplicationUtils.class);
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationUtils.applicationContext = applicationContext;
	}
	
    public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    } 
	
	/**
	 * 获取所有的接口实现类
	 * @param clazz
	 * @return
	 */
	public static <T> Map<String,T> getBeansOfType(Class<T> clazz) {
		Map<String,T> result = applicationContext.getBeansOfType(clazz);
		return result;
	}
	
	/**
	 * 获取指定的注解类
	 * @param clazz
	 * @return
	 */
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> clazz) {
    	Map<String, Object> beansWithAnnotationMap = applicationContext.getBeansWithAnnotation(clazz); 
    	return beansWithAnnotationMap;
    }
	
	/**
	 * 根据Class获取Bean
	 * @param clazz
	 * @return
	 */
    public static <T> T getBean(Class<T> clazz){
    	T result = applicationContext.getBean(clazz);
    	return result;
    }
    
    /**
     * 根据Name获取Bean
     * @param name
     * @return
     */
	public static Object getBean(String name) {
		return applicationContext != null?applicationContext.getBean(name):null;
	}
	
	public static String randomAll(int count) {
		return RandomStringUtils.random(count);
	}
	
	public static String randomAlpha(int count) {
		return RandomStringUtils.randomAlphabetic(count);
	}
	
	public static String randomNumber(int count) {
		return RandomStringUtils.randomNumeric(count);
	}
	
	public static String randomAlphaNumber(int count) {
		return RandomStringUtils.randomAlphanumeric(count);
	}
	
	/**
	 * 获取类实现的所有接口
	 * @param clazz
	 * @return
	 */
	public static Class<? extends Object>[] getInterfaces(Class<? extends Object> clazz) {
		Class<? extends Object>  [] interfaces = clazz.getInterfaces(); 
		return interfaces;
	}
	
	public static int getCpuCores(){
		int NCPUS = Runtime.getRuntime().availableProcessors();
		return NCPUS;
	}
	
	public static String getRootPath(){
		return System.getProperty("user.dir");
	}
	
	public static String getClassPathDir(){
		//return System.getProperty("java.class.path");
		return ApplicationUtils.class.getClassLoader().getResource("").getPath();
	}
	
	public static String getTMPDir(){
		//return System.getProperty("java.class.path");
		return getClassPathDir()+"tmp"+File.separator;
	}
	
	public static File getClassPathFile(String relativePath) throws FileNotFoundException{
		//File privatefile = ResourceUtils.getFile("classpath:rsa/keyprivate.txt");
		return ResourceUtils.getFile("classpath:"+relativePath);
	}
	
    public static String getLocalAddress() {
        try {
            // Traversal Network interface to get the first non-loopback and non-private address
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            ArrayList<String> ipv4Result = new ArrayList<String>();
            ArrayList<String> ipv6Result = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(normalizeHostAddress(address));
                        } else {
                            ipv4Result.add(normalizeHostAddress(address));
                        }
                    }
                }
            }

            // prefer ipv4
            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }

                    return ip;
                }

                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }
            //If failed to find,fall back to localhost
            final InetAddress localHost = InetAddress.getLocalHost();
            return normalizeHostAddress(localHost);
        } catch (Exception e) {
        	logger.error("Failed to obtain local address", e);
        }

        return null;
    }
    
    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }
	
    public static String getLocalIP() throws Exception {
        String ipString = "";
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1")) {
                	byte[] mac = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            		StringBuffer sb = new StringBuffer("");
            		for(int i=0; i<mac.length; i++) {
            			if(i!=0) {
            				sb.append("-");
            			}
            			//字节转换为整数
            			int temp = mac[i]&0xff;
            			String str = Integer.toHexString(temp);
            			if(str.length()==1) {
            				sb.append("0"+str);
            			}else {
            				sb.append(str);
            			}
            		}
                    return ip.getHostAddress();
                }
            }
        }
        return ipString;
    }
    
    public static String getHostName() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();  
        String hostName=addr.getHostName().toString(); //获取本机计算机名称  
        return hostName;
   }
    
	private static String getMacAddress(InetAddress ia) throws SocketException {
		// TODO Auto-generated method stub
		//获取网卡，获取地址
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
		StringBuffer sb = new StringBuffer("");
		for(int i=0; i<mac.length; i++) {
			if(i!=0) {
				sb.append("-");
			}
			//字节转换为整数
			int temp = mac[i]&0xff;
			String str = Integer.toHexString(temp);
			System.out.println("每8位:"+str);
			if(str.length()==1) {
				sb.append("0"+str);
			}else {
				sb.append(str);
			}
		}
		return sb.toString().toUpperCase();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(ApplicationUtils.getLocalIP());
		System.out.println(ApplicationUtils.getLocalAddress());
		System.out.println(ApplicationUtils.getHostName());
		
		System.out.println(randomAlpha(5).toUpperCase());
	}
}
