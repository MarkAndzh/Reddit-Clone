package com.example.redditclone;

import com.example.redditclone.config.RsaKeyProperties;
import com.example.redditclone.repository.UserRepository;
import com.example.redditclone.service.UserDetailsServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(RsaKeyProperties.class)
public class RedditCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedditCloneApplication.class, args);
	}

}
