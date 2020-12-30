package com.cos.reflect.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.reflect.anno.RequestMapping;
import com.cos.reflect.controller.UserController;

// 분기 시키기
public class Dispatcher implements Filter {

	private boolean isMatching = false;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// System.out.println("컨텍스트패스 : " + request.getContextPath()); // 프로젝트 시작주소
		// System.out.println("식별자주소 : " + request.getRequestURI()); // 끝주소
		// System.out.println("전체주소 : " + request.getRequestURL()); // 전체주소

		// /user 파싱하기
		String endPoint = request.getRequestURI().replaceAll(request.getContextPath(), "");
		System.out.println("엔드포인트 : " + endPoint); // /user/login

		UserController userController = new UserController();
//		if(endPoint.equals("/join")) {
//			userController.join();
//		}else if(endPoint.equals("/login")) {
//			userController.login();
//		}else if(endPoint.equals("/user")) {
//			userController.user();
//		}

		// 리플렉션 -> 메서드를 런타임 시점에서 찾아내서 실행
		Method[] methods = userController.getClass().getDeclaredMethods(); // 그 파일에 메서드만!!
//		for (Method method : methods) {
//			//System.out.println(method.getName());
//			if(endPoint.equals("/"+method.getName())) {
//				try {
//					method.invoke(userController);
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			}
//		}

		for (Method method : methods) { // 4바퀴 (join, login, user, hello)
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation;
			// System.out.println(requestMapping.value());

			if (requestMapping.value().equals(endPoint)) {
				isMatching = true;
				try {
					Parameter[] params = method.getParameters();
					String path = null;
					if (params.length != 0) {
						// System.out.println("params[0].getType() : "+params[0].getType());
						Object dtoInstance = params[0].getType().newInstance(); // /user/login => LoginDto, /user/join
																				// => JoinDto
						setData(dtoInstance, request);
//						String username = request.getParameter("username");
//						String password = request.getParameter("password");
//						System.out.println("username : " + username);
//						System.out.println("password : " + password);
						path = (String) method.invoke(userController, dtoInstance);
						// keys 값을 변형 username => setUsername
						// keys 값을 변형 password => setPassword
					} else {
						path = (String) method.invoke(userController);
					}

					RequestDispatcher dis = request.getRequestDispatcher(path); // 필터를 다시 안탐!!
					dis.forward(request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	private <T> void setData(T instance, HttpServletRequest request) {
		Enumeration<String> keys = request.getParameterNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String methodKey = keyToMethodKey(key);
			
			Method[] methods = instance.getClass().getDeclaredMethods();
			
			for (Method method : methods) {
				if(method.getName().equals(methodKey)) {
					try {
						method.invoke(instance, request.getParameter(key));
					} catch (Exception e) {
						try {
							int value = Integer.parseInt(request.getParameter(key));
							method.invoke(instance, value);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private String keyToMethodKey (String key) {
		String firstKey = "set";
		String upperKey = key.substring(0,1).toUpperCase();
		String remainKey = key.substring(1);
		
		return firstKey + upperKey + remainKey;
	}
}