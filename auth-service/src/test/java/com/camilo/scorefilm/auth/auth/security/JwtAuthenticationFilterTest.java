package com.camilo.scorefilm.auth.auth.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final ExposedFilter filter = new ExposedFilter(jwtService, userDetailsService);

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void shouldNotFilter_jwksRequest_returnsTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/.well-known/jwks.json");
        request.setServletPath("/.well-known/jwks.json");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void doFilter_missingHeader_continuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        FilterChain chain = mock(FilterChain.class);

        filter.run(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(eq(request), any());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void doFilter_validToken_authenticatesUserAndContinues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.addHeader("Authorization", "Bearer token");
        FilterChain chain = mock(FilterChain.class);
        var details = User.withUsername("camilo").password("hash").roles("USER").build();
        when(jwtService.extractUsername("token")).thenReturn("camilo");
        when(userDetailsService.loadUserByUsername("camilo")).thenReturn(details);
        when(jwtService.isTokenValid("token", details)).thenReturn(true);

        filter.run(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("camilo");
        verify(chain).doFilter(eq(request), any());
    }

    @Test
    void doFilter_invalidToken_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.addHeader("Authorization", "Bearer token");
        FilterChain chain = mock(FilterChain.class);
        var details = User.withUsername("camilo").password("hash").roles("USER").build();
        when(jwtService.extractUsername("token")).thenReturn("camilo");
        when(userDetailsService.loadUserByUsername("camilo")).thenReturn(details);
        when(jwtService.isTokenValid("token", details)).thenReturn(false);

        filter.run(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(eq(request), any());
    }

    @Test
    void doFilter_malformedToken_continuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.addHeader("Authorization", "Bearer malformed");
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("malformed")).thenThrow(new IllegalArgumentException("bad token"));

        filter.run(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(eq(request), any());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_existingAuthentication_doesNotReloadUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("existing", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.addHeader("Authorization", "Bearer token");
        FilterChain chain = mock(FilterChain.class);
        when(jwtService.extractUsername("token")).thenReturn("camilo");

        filter.run(request, new MockHttpServletResponse(), chain);

        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("existing");
    }

    private static final class ExposedFilter extends JwtAuthenticationFilter {
        private ExposedFilter(JwtService jwtService, UserDetailsService userDetailsService) {
            super(jwtService, userDetailsService);
        }
        private void run(MockHttpServletRequest request, MockHttpServletResponse response, FilterChain chain)
                throws Exception { doFilterInternal(request, response, chain); }
    }
}
