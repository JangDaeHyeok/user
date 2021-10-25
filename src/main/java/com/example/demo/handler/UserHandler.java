package com.example.demo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//MVC service 역할
//DB 연결 후 데이터 전송할 곳
@RequiredArgsConstructor
@Component
@Log4j2
public class UserHandler {
	
	private static final String USER_CIRCUIT_BREAKER = "userCircuitBreaker";

	@Autowired
	UserRepository userRepository;

	// user list by multi user_id
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> userSelectByUserIds(ServerRequest request) {
//		Mono<List<Map<Object,Object>>> acceptData = request.bodyToMono(List.class);
		Mono<List> acceptData = request.bodyToMono(List.class);
		return acceptData.flatMap(s -> {
			// 서킷브레이커 테스트용
//			if(true) {
//				return Mono.error(new RuntimeException("failed"));
//			}
			List<Map<Object, Object>> list = s;
			List<String> userIdList = new ArrayList<>();
			for (int i = 0; i < s.size(); i++) {
				if(list.get(i).get("userId") != null) {
					if(!userIdList.contains(list.get(i).get("userId"))) {
						userIdList.add(list.get(i).get("userId").toString());
					}
				}
			}
			if(userIdList.size() > 0) {
				Mono<List<User>> userList = userRepository.findByUsersId(userIdList).collectList();
				return userList.flatMap(u -> {
					List<User> returnList = u;
					return ServerResponse.ok().body(Mono.just(returnList), Flux.class);
				});
			}
			else {
				return ServerResponse.ok().body(Mono.just(userIdList), Flux.class);
			}
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// user one by multi user_id
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> userSelectByUserIdOne(ServerRequest request) {
		Mono<Map> acceptData = request.bodyToMono(Map.class);
		return acceptData.flatMap(s -> {
			// 서킷브레이커 테스트용
//				if(true) {
//					return Mono.error(new RuntimeException("failed"));
//				}
			List<String> userIdList = new ArrayList<>();
			if(s.get("userId") != null) {
				userIdList.add(s.get("userId").toString());
			}
			Mono<List<User>> userList = userRepository.findByUsersId(userIdList).collectList();
			return userList.flatMap(u -> {
				List<User> returnList = u;
				return ServerResponse.ok().body(Mono.just(returnList), Flux.class);
			});
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// circuitbreaker fallback
	public Mono<ServerResponse> fallback(ServerRequest request, Throwable t) {
        log.error("Fallback : " + t.getMessage());
        return ServerResponse.ok().body(Mono.just("error"), String.class);
    }

}
