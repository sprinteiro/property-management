package org.propertymanagement.associationmeeting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
public class WebSecurityConfig {
    public static final String ROLE_COMMUNITY_MEMBER = "COMMUNITY_MEMBER";
    public static final String ROLE_COMMUNITY_PRESIDENT = "COMMUNITY_PRESIDENT";
    public static final String ROLE_COMMUNITY_VICEPRESIDENT = "COMMUNITY_VICEPRESIDENT";
    public static final String ROLE_COMMUNITY_ADMIN = "COMMUNITY_ADMIN";
    public static final String ROLE_SUPERADMIN = "SUPERADMIN";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(HttpMethod.GET, "/test/notifications/lookup")
                            .hasRole(ROLE_COMMUNITY_ADMIN));
        // @formatter:off
        http.authorizeHttpRequests((authz) -> authz
                        .requestMatchers(HttpMethod.GET, "/test/notifications/**")
                            .hasAnyRole(ROLE_COMMUNITY_MEMBER, ROLE_COMMUNITY_VICEPRESIDENT, ROLE_COMMUNITY_PRESIDENT, ROLE_COMMUNITY_ADMIN, ROLE_SUPERADMIN)
                        .requestMatchers(HttpMethod.POST, "/test/notifications/**")
                            .hasAnyRole(ROLE_COMMUNITY_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/communities/**")
                            .hasAnyRole(ROLE_COMMUNITY_MEMBER, ROLE_COMMUNITY_VICEPRESIDENT, ROLE_COMMUNITY_PRESIDENT, ROLE_COMMUNITY_ADMIN, ROLE_SUPERADMIN)
                        // New meeting invite schedule
                        .requestMatchers(HttpMethod.POST, "/communities/{communityId}/meetings")
                            .hasAnyRole(ROLE_COMMUNITY_ADMIN, ROLE_SUPERADMIN)
                        // Resend meeting invite schedule
                        .requestMatchers(HttpMethod.POST, "/communities/resendinvite")
                            .hasAnyRole(ROLE_COMMUNITY_ADMIN, ROLE_SUPERADMIN)
                        // Approve meeting schedule
                        .requestMatchers(HttpMethod.POST, "/communities/{communityId}/trackers/{trackerId}")
                            .hasAnyRole(ROLE_COMMUNITY_PRESIDENT, ROLE_SUPERADMIN)
                        .anyRequest().denyAll())
//                 No session
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
//                 HTTP Basic authentication
                .httpBasic(withDefaults())
//                 API is stateless, no CSRF token
                .csrf(CsrfConfigurer::disable);
        // @formatter:on
        return http.build();
    }


    @Profile(value = { "h2" })
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        // These URLs pass straight through, no checks
        return web -> web.ignoring().requestMatchers("/h2-console/**");
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails markUser = User.withUsername("louis").password(passwordEncoder.encode("louis"))
                .roles(ROLE_COMMUNITY_MEMBER).build();
        UserDetails louisUser = User.withUsername("mark").password(passwordEncoder.encode("mark"))
                .roles(ROLE_COMMUNITY_MEMBER).build();
        UserDetails presidentUser = User.withUsername("president").password(passwordEncoder.encode("president"))
                .roles(ROLE_COMMUNITY_MEMBER, ROLE_COMMUNITY_PRESIDENT).build();
        UserDetails vicepresidentUser = User.withUsername("vicepresident").password(passwordEncoder.encode("president"))
                .roles(ROLE_COMMUNITY_MEMBER, ROLE_COMMUNITY_VICEPRESIDENT).build();
        UserDetails admin = User.withUsername("admin").password(passwordEncoder.encode("admin"))
                .roles(ROLE_COMMUNITY_ADMIN).build();
        UserDetails superadmin = User.withUsername("superadmin").password(passwordEncoder.encode("superadmin"))
                .roles(ROLE_SUPERADMIN).build();

        return new InMemoryUserDetailsManager(markUser, louisUser, presidentUser, vicepresidentUser, admin, superadmin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
