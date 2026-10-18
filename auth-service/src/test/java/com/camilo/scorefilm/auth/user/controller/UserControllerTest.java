package com.camilo.scorefilm.auth.user.controller;

import com.camilo.scorefilm.auth.auth.security.CustomUserDetails;
import com.camilo.scorefilm.auth.user.dto.UserResponse;
import com.camilo.scorefilm.auth.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {
    private final UserService userService = mock(UserService.class);
    private final UserController controller = new UserController(userService);

    @Test
    void getCurrentUser_customPrincipal_returnsUserDataAndSelfLink() {
        var details = CustomUserDetails.from(com.camilo.scorefilm.auth.user.entity.User.builder()
                .username("camilo").passwordHash("hash").enabled(true).build());
        var authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        when(userService.getUserByUsername("camilo")).thenReturn(UserResponse.builder().username("camilo").build());

        var response = controller.getCurrentUser(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData().getUsername()).isEqualTo("camilo");
        assertThat(response.getBody().getData().getLink("self")).isPresent();
    }

    @Test
    void getCurrentUser_unexpectedPrincipal_returnsInternalServerError() {
        var authentication = new UsernamePasswordAuthenticationToken("not-details", null);

        var response = controller.getCurrentUser(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("interno");
    }

    @Test
    void adminOnly_authenticatedUser_returnsOk() {
        var response = controller.adminOnly(new UsernamePasswordAuthenticationToken("admin", null));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData()).isEqualTo("admin");
    }

    @Test
    void userOrAdmin_authenticatedUser_returnsOk() {
        var response = controller.userOrAdmin(new UsernamePasswordAuthenticationToken("user", null));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getData()).isEqualTo("user");
    }
}
