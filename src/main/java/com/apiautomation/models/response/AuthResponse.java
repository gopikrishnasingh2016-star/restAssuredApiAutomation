package com.apiautomation.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Body of {@code POST /login} and {@code POST /register}. The same shape carries both the
 * happy path ({@code id}/{@code token}) and the failure path ({@code error}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(Integer id, String token, String error) {
}
