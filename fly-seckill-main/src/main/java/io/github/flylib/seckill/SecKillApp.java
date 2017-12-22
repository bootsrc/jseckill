package io.github.flylib.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("io.github.flylib.seckill.mapper")
public class SecKillApp {
    public static void main(String[] args) {
        SpringApplication.run(SecKillApp.class, args);
    }
}
