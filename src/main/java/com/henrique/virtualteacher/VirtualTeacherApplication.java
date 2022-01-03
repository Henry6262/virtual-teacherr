package com.henrique.virtualteacher;

import com.henrique.virtualteacher.entities.Course;
import com.henrique.virtualteacher.entities.Role;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.EnumRoles;
import com.henrique.virtualteacher.models.RegisterUserModel;
import com.henrique.virtualteacher.models.UserUpdateModel;
import com.henrique.virtualteacher.repositories.UserRepository;
import com.henrique.virtualteacher.services.implementation.UserServiceImpl;
import com.henrique.virtualteacher.services.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public class VirtualTeacherApplication {


    public static void main(String[] args) {
        SpringApplication.run(VirtualTeacherApplication.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.henrique.virtualteacher"))
                .build();
    }


}
