package com.example.demo.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)	// 기본 생성자의 접근 제어를 PROTECTED로 설정해놓게 되면 무분별한 객체 생성에 대해 한번 더 체크할 수 있는 수단
@Table("user_tb")
public class User {
	
	@Id	// pk
	private String userIdx;		// 유저 idx
	private String userId;		// 유저 id
	private String userName;	// 유저 이름
	private String userPw;		// 유저 비밀번호
	
	@Builder
	public User(String userIdx, String userId, String userName, String userPw) {
		this.userIdx = userIdx;
		this.userId = userId;
		this.userName = userName;
		this.userPw = userPw;
		
	}
}
