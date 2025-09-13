package com.gestionremodelacion.gestion.dto.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AuthResponse {

    private final String token;
    private final String type = "Bearer";
    private final Long id;
    private final String username;
    private final List<String> authorities;
    private final List<String> roles;
    private final Date expirationDate;
    private final String refreshToken;
    private final Long empresaId;
    private final String plan;
    private final String logoUrl;

    public AuthResponse(String token, Long id, String username, List<String> authorities, List<String> roles,
            Date expirationDate, String refreshToken, Long empresaId, String plan, String logoUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.authorities = authorities;
        this.roles = roles;
        this.expirationDate = expirationDate;
        this.refreshToken = refreshToken;
        this.empresaId = empresaId;
        this.plan = plan;
        this.logoUrl = logoUrl;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public String getExpirationDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expirationDate);
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getPlan() {
        return plan;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}
