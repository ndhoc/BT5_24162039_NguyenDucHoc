package com.nguyenduchoc.bt5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.DisposableBean;
import com.nguyenduchoc.bt5.repository.JpaConfig;

@SpringBootApplication
public class BT5_24162039_NguyenDucHocApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        // The legacy JpaConfig factory is not compatible with DevTools' restart classloader.
        if (System.getProperty("spring.devtools.restart.enabled") == null)
            System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(BT5_24162039_NguyenDucHocApplication.class, args);
    }
    @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) { return builder.sources(BT5_24162039_NguyenDucHocApplication.class); }
    @Bean DisposableBean closeJpaFactory() { return JpaConfig::shutdown; }
}
