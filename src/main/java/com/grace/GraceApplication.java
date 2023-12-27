package com.grace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * graceApplication
 *
 * @author chanfa
 */
@SpringBootApplication
@Slf4j
public class GraceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraceApplication.class, args);
        log.info("------------grace-boot 启动成功！！！----------------");
    }
}
