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
                .antMatchers("/resources/**","/static/**", "/css/**", "/js/**", "**/.js")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/api/courses", "/api/courses/{id}")
                .hasAnyRole("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.POST, "/api/courses/{id}/rate", "/api/courses/{id}/complete", "/api/courses/{id}/enroll",
                            "/api/courses/{id}/lecture/{entryId}", "/api/courses/{id}/lecture/{entryId}/submit")
                .hasRole("USER")
                .antMatchers(HttpMethod.GET, "/api/courses/{id}/lecture/{entryId}", "/api/courses/enabled", "/api/courses/{id}")
                .hasRole("USER")
                .antMatchers(HttpMethod.DELETE, "/api/courses/{id}")
                .hasAnyRole("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.PUT, "/api/courses/{id}/update")
                .hasAnyRole("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.GET, "/api/courses/disabled", "/api/courses/all")
                .hasAnyRole("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.POST, "/api/courses/enable", "/api/courses/disable")
                .hasAnyRole("ADMIN", "TEACHER")

                .antMatchers(HttpMethod.GET,"/api/users/{id}", "/api/users/search")
                .authenticated()
                .antMatchers(HttpMethod.GET, "/api/users/")
                .hasRole("TEACHER")
                .antMatchers(HttpMethod.POST, "/api/users/register")
                .permitAll()
                .antMatchers(HttpMethod.PUT, "/api/users/")
                .authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/users/")
                .authenticated()

                .antMatchers(HttpMethod.GET, "/api/lectures")
                .hasAnyRole("ADMIN", "TEACHER")
                .antMatchers(HttpMethod.GET, "/api/lectures/course/{id}", "api/lectures/{id}")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/api/lectures/{id}/update")
                .hasAnyRole("TEACHER", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/lecture/{id}/delete")
                .hasAnyRole("ADMIN", "TEACHER")

                .antMatchers(HttpMethod.GET, "/api/ratings/{id}")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/api/rating/{id}/update")
                .authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/rating/{id}/delete")
                .authenticated()

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
        web.ignoring().antMatchers( "/resources/**","/static/**", "/css/**", "/js/**", "**/.js/");
    }

    // "/resources/static/css/**/*", "/resources/static/**/*",
}
