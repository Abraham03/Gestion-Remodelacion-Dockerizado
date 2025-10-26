package com.gestionremodelacion.gestion.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.gestionremodelacion.gestion.security.exception.JwtAuthenticationEntryPoint;
import com.gestionremodelacion.gestion.security.jwt.JwtAuthFilter;
import com.gestionremodelacion.gestion.security.jwt.RateLimitFilter;

/**
 * Configuración centralizada de seguridad con: - Configuración CORS mejorada -
 * Protección CSRF - Manejo de excepciones - Políticas de sesión stateless -
 * Autorización basada en roles
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final RateLimitFilter rateLimitFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
            JwtAuthenticationEntryPoint unauthorizedHandler,
            UserDetailsService userDetailsService, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(EndpointRequest.to("health")).permitAll()
                        // --- Rutas protegidas por permisos/roles usando hasAuthority() ---
                        // Uso de HttpMethod.GET, HttpMethod.POST, etc. para evitar warnings futuros.

                        // Dashboard: requiere el permiso DASHBOARD_VIEW
                        .requestMatchers("/api/dashboard/**").hasAuthority("DASHBOARD_VIEW")
                        // Usuarios: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("USER_READ")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("USER_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("USER_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("USER_DELETE")
                        // Roles: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/roles/dropdown-by-empresa").hasAuthority("ROLE_DROPDOWN")
                        .requestMatchers(HttpMethod.GET, "/api/roles/**").hasAuthority("ROLE_READ")
                        .requestMatchers(HttpMethod.POST, "/api/roles/**").hasAuthority("ROLE_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/roles/**").hasAuthority("ROLE_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/roles/**").hasAuthority("ROLE_DELETE")
                        // Permisos: (si tuvieras un controlador para gestionar permisos)
                        .requestMatchers(HttpMethod.GET, "/api/permissions/dropdown")
                        .hasAuthority("PERMISSION_DROPDOWN")
                        .requestMatchers(HttpMethod.GET, "/api/permissions/**").hasAuthority("PERMISSION_READ")
                        .requestMatchers(HttpMethod.POST, "/api/permissions/**").hasAuthority("PERMISSION_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/permissions/**").hasAuthority("PERMISSION_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/permissions/**").hasAuthority("PERMISSION_DELETE")
                        // Proyectos: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/proyectos/**").hasAuthority("PROYECTO_READ")
                        .requestMatchers(HttpMethod.POST, "/api/proyectos/**").hasAuthority("PROYECTO_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/proyectos/**").hasAuthority("PROYECTO_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/proyectos/**").hasAuthority("PROYECTO_DELETE")
                        .requestMatchers(HttpMethod.GET, "/api/proyectos/dropdown")
                        .hasAuthority("PROYECTO_DROPDOWN")
                        // Clientes: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/clientes/**").hasAuthority("CLIENTE_READ")
                        .requestMatchers(HttpMethod.POST, "/api/clientes/**").hasAuthority("CLIENTE_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAuthority("CLIENTE_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasAuthority("CLIENTE_DELETE")
                        // Empleados: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/empleados/**").hasAuthority("EMPLEADO_READ")
                        .requestMatchers(HttpMethod.POST, "/api/empleados/**").hasAuthority("EMPLEADO_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/empleados/**").hasAuthority("EMPLEADO_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/empleados/{id}/status").hasAuthority("EMPLEADO_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/empleados/**").hasAuthority("EMPLEADO_DELETE")
                        .requestMatchers(HttpMethod.GET, "/api/empleados/dropdown").hasAuthority("EMPLEADO_DROPDOWN")
                        // Horas Trabajadas: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.GET, "/api/horas-trabajadas/**")
                        .hasAuthority("HORASTRABAJADAS_READ")
                        .requestMatchers(HttpMethod.POST, "/api/horas-trabajadas/**")
                        .hasAuthority("HORASTRABAJADAS_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/horas-trabajadas/**")
                        .hasAuthority("HORASTRABAJADAS_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/horas-trabajadas/**")
                        .hasAuthority("HORASTRABAJADAS_DELETE")
                        // Empresas: cada operación requiere un permiso específico
                        .requestMatchers(HttpMethod.POST, "/api/empresas/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/empresas/**").hasRole("SUPER_ADMIN")
                        // solicitud para invitar a un empleado a una empresa
                        .requestMatchers(HttpMethod.POST, "/api/invitations/**").hasAuthority("INVITE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/invitations/validate").permitAll()
                        // Cualquier otra solicitud debe estar autenticada
                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitFilter, LogoutFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "Skip-Interceptor"));
        config.setExposedHeaders(List.of("Authorization", "X-Refresh-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Este filtro se registra con la máxima prioridad para asegurar que se ejecute
    // siempre.

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
                new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
