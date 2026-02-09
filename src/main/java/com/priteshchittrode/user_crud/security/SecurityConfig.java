package com.priteshchittrode.user_crud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // ❌ Disable default security stuff
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()

                // ✅ Stateless JWT-based auth
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // ✅ Public & protected APIs
                .authorizeRequests()
                .antMatchers(
                        "/api/auth/sign-up",
                        "/api/auth/sign-in",
                        "/api/auth/refresh-token/**"
                ).permitAll()
                .anyRequest().authenticated();

        // ✅ JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // ✅ Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
