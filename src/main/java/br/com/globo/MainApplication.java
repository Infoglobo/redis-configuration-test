package br.com.globo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {

    public static final String PROFILE_STANDALONE = "redis-standalone";
    public static final String PROFILE_CLUSTER = "redis-cluster";
    public static final String PROFILE_NO_REDIS = "no-redis";

    public static void main(String[] args) {

        SpringApplication.run(MainApplication.class, args);
    }
}
