package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

@RestController
public class TestController {
	
	@RequestMapping(value = { "/user/test"})
    public void logout(WebSession session) {
		System.out.println(session.getId());
    }

}
