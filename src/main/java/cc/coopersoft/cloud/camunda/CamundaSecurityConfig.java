package cc.coopersoft.cloud.camunda;

import cc.coopersoft.cloud.camunda.rest.AudienceValidator;
import cc.coopersoft.cloud.camunda.rest.KeycloakAuthenticationFilter;
import cc.coopersoft.cloud.camunda.rest.RestApiSecurityConfigurationProperties;
import cc.coopersoft.cloud.camunda.sso.KeycloakLogoutHandler;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Collections;

/**
 * Optional Security Configuration for Camunda REST Api.
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER - 20)
public class CamundaSecurityConfig {

  private final KeycloakLogoutHandler keycloakLogoutHandler;

  /**
   * Configuration for REST Api security.
   */
  private final RestApiSecurityConfigurationProperties configProps;

  /**
   * Access to Camunda's Identity Service.
   */
  private final IdentityService identityService;

  /**
   * Access to Spring Security OAuth2 client service.
   */
  private final OAuth2AuthorizedClientService clientService;

  private final ApplicationContext applicationContext;

  public CamundaSecurityConfig(ApplicationContext applicationContext, OAuth2AuthorizedClientService clientService, IdentityService identityService, RestApiSecurityConfigurationProperties configProps, KeycloakLogoutHandler keycloakLogoutHandler) {
    this.applicationContext = applicationContext;
    this.clientService = clientService;
    this.identityService = identityService;
    this.configProps = configProps;
    this.keycloakLogoutHandler = keycloakLogoutHandler;
  }

  /**
   * {@inheritDoc}
   */

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    String jwkSetUri = applicationContext.getEnvironment().getRequiredProperty(
        "spring.security.oauth2.client.provider." + configProps.getProvider() + ".jwk-set-uri");

    http
        .csrf().ignoringAntMatchers("/api/**", "/engine-rest/**");

    http.antMatcher("/engine-rest/**")
            .authorizeRequests().antMatchers("/**")
                .authenticated()
            .and()
                .oauth2ResourceServer()
                    .jwt().jwkSetUri(jwkSetUri);


    http
        .requestMatchers().antMatchers ("/**").and()
        .authorizeRequests(
            authorizeRequests ->
                authorizeRequests
                    .antMatchers("/app/**", "/api/**", "/lib/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll()
        )
        .oauth2Login()
        .and()
        .logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/app/**/logout"))
        .logoutSuccessHandler(keycloakLogoutHandler)
    ;

    return http.build();
  }

  /**
   * Create a JWT decoder with issuer and audience claim validation.
   *
   * @return the JWT decoder
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    String issuerUri = applicationContext.getEnvironment().getRequiredProperty(
        "spring.security.oauth2.client.provider." + configProps.getProvider() + ".issuer-uri");

    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(configProps.getRequiredAudience());
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }

  /**
   * Registers the REST Api Keycloak Authentication Filter.
   *
   * @return filter registration
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticationFilter() {
    FilterRegistrationBean filterRegistration = new FilterRegistrationBean();

    String userNameAttribute = this.applicationContext.getEnvironment().getRequiredProperty(
        "spring.security.oauth2.client.provider." + this.configProps.getProvider() + ".user-name-attribute");

    filterRegistration.setFilter(new KeycloakAuthenticationFilter(this.identityService, this.clientService, userNameAttribute));

    filterRegistration.setOrder(102); // make sure the filter is registered after the Spring Security Filter Chain
    filterRegistration.addUrlPatterns("/engine-rest/*");
    return filterRegistration;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Bean
  public FilterRegistrationBean containerBasedAuthenticationFilter(){

    FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
    filterRegistration.setFilter(new ContainerBasedAuthenticationFilter());
    filterRegistration.setInitParameters(Collections.singletonMap("authentication-provider", "cc.coopersoft.cloud.camunda.sso.KeycloakAuthenticationProvider"));
    filterRegistration.setOrder(101); // make sure the filter is registered after the Spring Security Filter Chain
    filterRegistration.addUrlPatterns("/app/*");
    return filterRegistration;
  }

  @Bean
  public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
    FilterRegistrationBean<ForwardedHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new ForwardedHeaderFilter());
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
  }

  @Bean
  @Order(0)
  public RequestContextListener requestContextListener() {
    return new RequestContextListener();
  }

}
