package org.ezhixuan.xuan_picture_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class XuanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuanPictureBackendApplication.class, args);
    }

}
