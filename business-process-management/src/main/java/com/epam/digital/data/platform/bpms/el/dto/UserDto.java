package com.epam.digital.data.platform.bpms.el.dto;

import com.epam.digital.data.platform.starter.security.dto.JwtClaimsDto;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Dto that represents structure with a username(mandatory) and user token(optional)
 * <p>
 * Provides getter for data that is stored in token
 */
@RequiredArgsConstructor
public class UserDto {

  @Getter
  private final String userName;
  @Getter
  private String accessToken;
  private JwtClaimsDto jwtClaimsDto;

  /**
   * Constructor for structure with filled token
   *
   * @param userName     preferredUserName of a user
   * @param token        current user token (may be {@code null})
   * @param jwtClaimsDto parsed user token claims dto (has to be parsed from the {@code token}, will
   *                     be {@code null} if {@code token} is {@code null}
   */
  public UserDto(String userName, String token, JwtClaimsDto jwtClaimsDto) {
    this(userName);
    this.accessToken = token;
    this.jwtClaimsDto = token == null ? null : Objects.requireNonNull(jwtClaimsDto);
  }

  public String getDrfo() {
    return jwtClaimsDto == null ? null : jwtClaimsDto.getDrfo();
  }

  public String getEdrpou() {
    return jwtClaimsDto == null ? null : jwtClaimsDto.getEdrpou();
  }

  public String getFullName() {
    return jwtClaimsDto == null ? null : jwtClaimsDto.getFullName();
  }

  public List<String> getRoles() {
    return jwtClaimsDto == null ? Collections.emptyList() : jwtClaimsDto.getRoles();
  }

  public String getSubjectType() {
    return jwtClaimsDto == null || jwtClaimsDto.getSubjectType() == null ? null
        : jwtClaimsDto.getSubjectType().name();
  }

  public Boolean isRepresentative() {
    return jwtClaimsDto == null ? null : jwtClaimsDto.isRepresentative();
  }
}
