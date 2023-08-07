package example.cashcard;

import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

// Lesson 3.1 - add @Configuration, removed in Spring Security 6.0
//@Configuration
@EnableWebSecurity

// Lesson 3.1 - Remove WebSecurityConfigurerAdapter
 public class SecurityConfig extends WebSecurityConfigurerAdapter {
//public class SecurityConfig {

    // Lesson 3.1 - replace configure method with SecurityFilterChain @Bean
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                authorizeRequests((authz) ->
                        authz
                                .requestMatchers(new AntPathRequestMatcher("/cashcards/**")).hasRole("CARD-OWNER")
                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                )
                .csrf().disable()
                .httpBasic(withDefaults());
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//
//                // Lesson 3.1 - replace AntPathRequestMatcher with .antMatchers
//                .requestMatchers(new AntPathRequestMatcher("/cashcards/**")).hasRole("CARD-OWNER")
//                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
////                .antMatchers("/cashcards/**").hasRole("CARD-OWNER")
////                .antMatchers("/h2-console/**").permitAll()
//
//                .and()
//                .csrf().disable()
//                .httpBasic();
//        return http.build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Lesson 3.1 - replace new passwordEncoder with factory method for version of Spring Security
        return new Pbkdf2PasswordEncoder();
//        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();
        UserDetails sarah = users
                .username("sarah1")
                .password(passwordEncoder.encode("abc123"))
                .roles("CARD-OWNER") // new role
                .build();
        UserDetails hankOwnsNoCards = users
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("qrs456"))
                .roles("NON-OWNER") // new role
                .build();
        UserDetails kumar = users
                .username("kumar2")
                .password(passwordEncoder.encode("xyz789"))
                .roles("CARD-OWNER")
                .build();
        return new InMemoryUserDetailsManager(sarah, hankOwnsNoCards, kumar);
    }
}