package com.henrique.virtualteacher.configurations;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/resources/**","/static/**", "/css/**", "**/js/", "/js/**", "**/.js")
                .permitAll()
                .antMatchers("/admins")
                .authenticated()



                .antMatchers(HttpMethod.POST, "/api/courses/image")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/courses/topics")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/api/courses", "/api/courses/{id}")
                .hasAnyAuthority("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.POST, "/api/courses/{id}/rate", "/api/courses/{id}/complete", "/api/courses/{id}/enroll",
                            "/api/courses/{id}/lecture/{entryId}", "/api/courses/{id}/lecture/{entryId}/submit")
                .hasAuthority("STUDENT")
                .antMatchers(HttpMethod.GET,  "/api/courses/enabled", "/api/courses/test")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/api/courses/{id}/lecture/{entryId}", "/api/courses/{id}")
                .hasAuthority("STUDENT")
                .antMatchers(HttpMethod.DELETE, "/api/courses/{id}")
                .hasAnyAuthority("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.PUT, "/api/courses/{id}/update")
                .hasAnyAuthority("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.GET, "/api/courses/disabled", "/api/courses/all")
                .hasAnyAuthority("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.POST, "/api/courses/enable", "/api/courses/disable")
                .hasAnyAuthority("ADMIN", "TEACHER")


                .antMatchers(HttpMethod.GET, "/api/users/search", "/api/users/login")
                .permitAll()
                .antMatchers(HttpMethod.GET,"/api/users/{id}")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/users")
                .hasAnyAuthority("TEACHER")
                .antMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login")
                .permitAll()
                .antMatchers(HttpMethod.PUT, "/api/users/")
                .authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/users")
                .authenticated()

                .antMatchers(HttpMethod.GET, "/api/lectures")
                .hasAnyAuthority("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.GET, "/api/lectures/course/{id}", "api/lectures/{id}")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/api/lectures/{id}/update")
                .hasAnyAuthority("TEACHER", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/lecture/{id}/delete")
                .hasAnyAuthority("ADMIN", "TEACHER")

                .antMatchers(HttpMethod.GET, "/api/ratings/{id}")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/api/rating/{id}/update")
                .authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/rating/{id}/delete")
                .authenticated()

                .antMatchers(HttpMethod.POST, "api/assignments/{id}/grade")
                .hasAnyAuthority("TEACHER", "ADMIN")

                .antMatchers(HttpMethod.POST, "/auth/login")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/auth/login")
                .permitAll()
                .and()

                .formLogin(login ->  login.permitAll()
                        .loginPage("/auth/login")
                        .successForwardUrl("/")
                        .permitAll())
                .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                //todo: in the future make pop up that shows when logout is completed saying logout successful
                .permitAll();



        http.httpBasic();

    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers( "/resources/**","/static/**", "/static/js/**", "/css/**", "/js/**", "**/.js/");
    }

    // "/resources/static/css/**/*", "/resources/static/**/*",
}
