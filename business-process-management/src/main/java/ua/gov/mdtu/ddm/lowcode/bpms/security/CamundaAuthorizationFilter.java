package ua.gov.mdtu.ddm.lowcode.bpms.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The class extends {@link OncePerRequestFilter} class and represents a filter that sets camunda
 * authorizations for authenticated user.
 */
@Component
@RequiredArgsConstructor
public class CamundaAuthorizationFilter extends OncePerRequestFilter {

  private final CamundaAuthProvider camundaAuthProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      camundaAuthProvider.createAuthentication(authentication);
      filterChain.doFilter(httpServletRequest, httpServletResponse);
    } finally {
      camundaAuthProvider.clearAuthentication();
    }
  }
}
