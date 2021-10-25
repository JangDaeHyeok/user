package com.example.demo.user;

import java.util.List;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String>{
	
	// user_id로 다수 특정회원 조회
	@Query("SELECT * FROM user_tb WHERE user_id IN (:userId)")
	Flux<User> findByUsersId(@Param("userId") List<String> userId);
	
	// user_id로 특정회원 조회
	@Query("SELECT * FROM user_tb WHERE user_id = :userId")
	Mono<User> findByUserId(String userId);
	
	// 로그인체크
	@Query("SELECT count(*) FROM user_tb WHERE user_id = :userId and user_pw = :userPw")
	Mono<Integer> checkLogin(String userId, String userPw);
	
	// 중복아이디 체크
	@Query("SELECT count(*) FROM user_tb WHERE user_id = :userId")
	Mono<Integer> checkUsersId(String userId);
	
	@Query("INSERT INTO user_tb (user_idx, user_id, user_name, user_pw) VALUES (:#{#user.userIdx}, :#{#user.userId}, :#{#user.userName}, :#{#user.userPw}) ")
	Mono<Integer> userJoin(@Param("user") User user);
	
}
