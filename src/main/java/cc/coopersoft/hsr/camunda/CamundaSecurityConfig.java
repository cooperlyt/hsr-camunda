package cc.coopersoft.hsr.camunda.rest;

import javax.inject.Inject;

import org.camunda.bpm.engine.IdentityService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Optional Security Configuration for Camunda REST Api.
 */
@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER - 20)
@ConditionalOnProperty(name = "rest.security.enabled", havingValue = "true", matchIfMissing = true)
public class RestApiSecurityConfig {

	/** Configuration for REST Api security. */
	private final RestApiSecurityConfigurationProperties configProps;
	
	/** Access to Camunda's Identity Service. */
	private final IdentityService identityService;
	
	/** Access to Spring Security OAuth2 client service. */
	private final OAuth2AuthorizedClientService clientService;

	private final ApplicationContext applicationContext;

	public RestApiSecurityConfig(ApplicationContext applicationContext, OAuth2AuthorizedClientService clientService, IdentityService identityService, RestApiSecurityConfigurationProperties configProps) {
		this.applicationContext = applicationContext;
		this.clientService = clientService;
		this.identityService = identityService;
		this.configProps = configProps;
	}

	/**
	 * {@inheritDoc}
	 */

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		String jwkSetUri = applicationContext.getEnvironment().getRequiredProperty(
				"spring.security.oauth2.client.provider." + configProps.getProvider() + ".jwk-set-uri");

		http
				.csrf().ignoringAntMatchers("/api/**", "/engine-rest/**")
				.and()
				.antMatcher("/engine-rest/**")
				.authorizeRequests()
				.anyRequest().authenticated()
				.and()
				.oauth2ResourceServer()
				.jwt().jwkSetUri(jwkSetUri)
		;
		return http.build();
	}
	/**
	 * Create a JWT decoder with issuer and audience claim validation.
	 * @return the JWT decoder
	 */
	@Bean
	public JwtDecoder jwtDecoder() {
		String issuerUri = applicationContext.getEnvironment().getRequiredProperty(
				"spring.security.oauth2.client.provider." + configProps.getProvider() + ".issuer-uri");
		
		NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
				JwtDecoders.fromOidcIssuerLocation(issuerUri);

		OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(configProps.getRequiredAudience());
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

		jwtDecoder.setJwtValidator(withAudience);

		return jwtDecoder;
	}
	   
    /**
     * Registers the REST Api Keycloak Authentication Filter.
     * @return filter registration
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Bean
    public FilterRegistrationBean keycloakAuthenticationFilter(){
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
		
		String userNameAttribute = this.applicationContext.getEnvironment().getRequiredProperty(
			"spring.security.oauth2.client.provider." + this.configProps.getProvider() + ".user-name-attribute");
    
    	filterRegistration.setFilter(new KeycloakAuthenticationFilter(this.identityService, this.clientService, userNameAttribute));
        
        filterRegistration.setOrder(102); // make sure the filter is registered after the Spring Security Filter Chain
        filterRegistration.addUrlPatterns("/engine-rest/*");
        return filterRegistration;
    }
   
}
