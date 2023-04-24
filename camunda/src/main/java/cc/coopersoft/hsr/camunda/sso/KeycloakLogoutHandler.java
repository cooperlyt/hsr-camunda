package cc.coopersoft.hsr.camunda.sso;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Service
public class KeycloakLogoutHandler implements LogoutSuccessHandler {


  /** Redirect strategy. */
  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  /** Keycloak's logout URI. */
  private String oauth2UserLogoutUri;

  /**
   * Default constructor.
   * @param oauth2UserAuthorizationUri configured keycloak authorization URI
   */
  public KeycloakLogoutHandler(@Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri:}") String oauth2UserAuthorizationUri) {
    if (!StringUtils.isEmpty(oauth2UserAuthorizationUri)) {
      // in order to get the valid logout uri: simply replace "/auth" at the end of the user authorization uri with "/logout"
      this.oauth2UserLogoutUri = oauth2UserAuthorizationUri.replace("openid-connect/auth", "openid-connect/logout");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    if (!StringUtils.isEmpty(oauth2UserLogoutUri)) {
      // Calculate redirect URI for Keycloak, something like http://<host:port>/camunda
      String requestUrl = request.getRequestURL().toString();
      String redirectUri = requestUrl.substring(0, requestUrl.indexOf("/app"));
      // Complete logout URL
      String logoutUrl = oauth2UserLogoutUri + "?post_logout_redirect_uri=" + redirectUri + "&id_token_hint=" + ((OidcUser)authentication.getPrincipal()).getIdToken().getTokenValue();

      // Do logout by redirecting to Keycloak logout
      log.debug("Redirecting to logout URL {}", logoutUrl);
      redirectStrategy.sendRedirect(request, response, logoutUrl);
    }
  }

}