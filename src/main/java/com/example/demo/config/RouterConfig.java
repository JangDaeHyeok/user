package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.handler.UserHandler;
import com.example.demo.handler.UserLoginHandler;

@Configuration
public class RouterConfig {

	@Bean
    public RouterFunction<ServerResponse> userRouter(UserHandler handler) {
        return RouterFunctions.route()
                .POST("/user/list/userNm", handler::userSelectByUserIds)
                .POST("/user/list/userNmOne", handler::userSelectByUserIdOne)
                .build(); 
    }
	
	@Bean
	public RouterFunction<ServerResponse> userLoginRouter(UserLoginHandler handler) {
		return RouterFunctions.route()
				.POST("/user/login", handler::userLogin)
				.POST("/user/join", handler::userJoin)
				.POST("/user/checkId", handler::checkUserId)
				.POST("/user/logout", handler::userLogout)
				.POST("/user/checkSession", handler::checkSession)
				.build(); 
	}
}
