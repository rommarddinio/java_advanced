package com.innowise.userservice.filter;

import com.innowise.userservice.details.UserDetailsImpl;
import com.innowise.userservice.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenFilterTest {

    private TokenService tokenService;

    private TokenFilter tokenFilter;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private FilterChain filterChain;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        tokenService = mock(TokenService.class);
        tokenFilter = new TokenFilter(tokenService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void shouldSkipFilter_whenSecurityContextAlreadySet() throws Exception {
        Authentication auth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(auth);

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipFilter_onPostUsers() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/users");

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReturn401_whenHeaderMissingOrInvalid() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn(null);

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Authorization header missing or invalid"));
    }

    @Test
    void shouldSetSecurityContext_whenTokenValid() throws Exception {
        String token = "valid-token";

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(tokenService.getUserId(token)).thenReturn(1L);
        when(tokenService.getRole(token)).thenReturn("USER");
        when(tokenService.getTokenType(token)).thenReturn("ACCESS");

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(UserDetailsImpl.class, authentication.getPrincipal().getClass());

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturn401_whenTokenExpired() throws Exception {
        String token = "expired-token";

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.getUserId(token)).thenThrow(ExpiredJwtException.class);

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Token is expired"));
    }

    @Test
    void shouldReturn401_whenTokenInvalid() throws Exception {
        String token = "invalid-token";

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(tokenService.getUserId(token)).thenThrow(SignatureException.class);
        when(tokenService.getTokenType(token)).thenReturn("ACCESS");

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(responseWriter.toString().contains("Invalid token"));
    }

    @Test
    void shouldReturn400_whenTokenTypeInvalid() throws Exception {
        String token = "refresh-token";

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/other");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(tokenService.getUserId(token)).thenReturn(1L);
        when(tokenService.getRole(token)).thenReturn("USER");
        when(tokenService.getTokenType(token)).thenReturn("REFRESH");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        tokenFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}

