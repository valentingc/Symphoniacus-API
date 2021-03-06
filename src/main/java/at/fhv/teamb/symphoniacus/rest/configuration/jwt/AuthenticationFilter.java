package at.fhv.teamb.symphoniacus.rest.configuration.jwt;

import at.fhv.teamb.symphoniacus.rest.models.CustomResponse;
import at.fhv.teamb.symphoniacus.rest.models.CustomResponseBuilder;
import at.fhv.teamb.symphoniacus.rest.service.AuthenticationService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Optional;

/**
 * This filter Class will filter each Request to an API Endpoint that have the @Secure annotation.
 * The filter will look if there is a valid Json Web Token in the Authorization Header
 * of the Request. The Request will be redirected if the Token is Signed with our key and the
 * and the expiry date has not expired.
 *
 * @author Tobias Moser
 **/

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Logger LOG = LogManager.getLogger(AuthenticationFilter.class);
    private static final String REALM = "example";
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader
                .substring(AUTHENTICATION_SCHEME.length()).trim();

        try {

            // Validate the token
            String userId = validateToken(token);

            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    return () -> userId;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return AUTHENTICATION_SCHEME;
                }
            });

        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {

        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        CustomResponse<Void> errorResponse =
                new CustomResponseBuilder<Void>("Client Failure", 401)
                        .withMessage("Not authorized for this action.")
                .build();

        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .type("text/json")
                        .entity(errorResponse)
                        .build());
    }

    /**
     * Check if the token was issued by the server. Throw an Exception if the token is invalid.
     * @param token given from the User.
     */
    private String validateToken(String token) throws Exception {
        Optional<SignedJWT> signedJwtOpt = new AuthenticationService().verifyToken(token);
        boolean verifiedSignature = signedJwtOpt.isPresent();

        if (!verifiedSignature) {
            throw new Exception();
        }
        SignedJWT signedJwt = signedJwtOpt.get();
        JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
        Long userId = (Long) claims.getClaim("userId");

        return String.valueOf(userId);
    }
}