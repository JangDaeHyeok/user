package com.example.demo.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.util.UUIDUtil;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Log4j2
public class UserLoginHandler {
	
	private static final String USER_CIRCUIT_BREAKER = "userCircuitBreaker";
	
	@Autowired
	UserRepository userRepository;
	
	// 로그인
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> userLogin(ServerRequest request) {
		Mono<WebSession> session = request.session();
		Mono<User> acceptData = request.bodyToMono(User.class);
		return acceptData.flatMap(s -> {
			Mono<Integer> checkLogin = userRepository.checkLogin(s.getUserId(), s.getUserPw());
			return checkLogin.flatMap(r -> {
				if(r > 0) {
					Mono<User> one = userRepository.findByUserId(s.getUserId());
					
					return Mono.zip(one, session).flatMap(dd -> {
						// session에 정보 주입
						dd.getT2().getAttributes().put("userIdx", dd.getT1().getUserIdx());
						dd.getT2().getAttributes().put("userId", dd.getT1().getUserId());
						dd.getT2().getAttributes().put("userName", dd.getT1().getUserName());
						
						Map<String, String> returnMap = new HashMap<>();
						returnMap.put("result", "success");
						returnMap.put("author", dd.getT2().getId());
						return ServerResponse.ok()
								.body(Mono.just(returnMap), Map.class);
					});
				}
				else {
					return ServerResponse.ok().body(Mono.just("fail"), String.class);
				}
			});
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// 회원가입
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> userJoin(ServerRequest request) {
		Mono<User> acceptData = request.bodyToMono(User.class);
		return acceptData.flatMap(s -> {
			User user = User.builder()
						.userIdx(UUIDUtil.createUUID())
						.userId(s.getUserId())
						.userName(s.getUserName())
						.userPw(s.getUserPw())
						.build();
			Mono<Integer> userJoin = userRepository.userJoin(user);
			return userJoin.flatMap(r -> {
				if(r > 0) {
					return ServerResponse.ok().body(Mono.just("success"), String.class);
				}
				else {
					return ServerResponse.ok().body(Mono.just("fail"), String.class);
				}
			});
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// id 중복체크
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> checkUserId(ServerRequest request) {
		Mono<User> acceptData = request.bodyToMono(User.class);
		return acceptData.flatMap(s -> {
			Mono<Integer> userJoin = userRepository.checkUsersId(s.getUserId());
			return userJoin.flatMap(r -> {
				if(r < 1) {
					return ServerResponse.ok().body(Mono.just("success"), String.class);
				}
				else {
					return ServerResponse.ok().body(Mono.just("fail"), String.class);
				}
			});
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// 로그아웃
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> userLogout(ServerRequest request) {
		Mono<WebSession> session = request.session();
		return session.flatMap(s -> {
			s.invalidate().subscribe();
			return ServerResponse.ok().body(Mono.just("success"), String.class);
		})
		.onErrorResume(err -> {
			err.printStackTrace();
			return Mono.error(err);
		});
	}
	
	// 세션체크
	@CircuitBreaker(name = USER_CIRCUIT_BREAKER, fallbackMethod = "fallback")
	public Mono<ServerResponse> checkSession(ServerRequest request) {
		Mono<WebSession> session = request.session();
		return session.flatMap(t -> {
			if(t.getAttribute("userId") != null) {
				User user = User.builder()
							.userIdx(t.getAttribute("userIdx").toString())
							.userId(t.getAttribute("userId").toString())
							.userName(t.getAttribute("userName").toString())
							.build();
				
				return ServerResponse.ok().body(Mono.just(user), User.class);
			}
			else {
				return ServerResponse.ok().body(Mono.just("logout"), String.class);
			}
		});
	}
	
	// circuitbreaker fallback
	public Mono<ServerResponse> fallback(ServerRequest request, Throwable t) {
        log.error("Fallback : " + t.getMessage());
        return ServerResponse.ok().body(Mono.just("error"), String.class);
    }
}
