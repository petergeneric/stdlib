package com.peterphi.usermanager.rest.iface.oauth2server;

import com.peterphi.std.annotation.Doc;
import com.peterphi.usermanager.rest.type.UserManagerUser;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/oauth2")
@Doc("Implements the OAuth2 flow")
public interface UserManagerOAuthService
{
	@GET
	@Path("/authorize")
	Response getAuth(@QueryParam("response_type") @Doc("The response type - N.B. only code supported") String responseType,
	                 @QueryParam("client_id") @Doc("Client identifier") final String clientId,
	                 @QueryParam("redirect_uri") @Doc("client endpoint to return to") String redirectUri,
	                 @QueryParam("state") @Doc("optional state to return to client at end of flow") String state,
	                 @QueryParam("scope") @Doc("optional scope") String scope);


	@POST
	@Path("/authorize")
	Response userMadeAuthDecision(@FormParam("response_type") @Doc("Original response_type") String responseType,
	                              @FormParam("client_id") @Doc("Original client_id") final String clientId,
	                              @FormParam("redirect_uri") @Doc("Original redirect_uri") String redirectUri,
	                              @FormParam("state") @Doc("Original state") String state,
	                              @FormParam("scope") @Doc("Original scope") String scope,
	                              @FormParam("nonce") @Doc("For CSRF prevention") String nonce,
	                              @FormParam("decision") @Doc("decision (Allow/Deny)") String decision);

	/**
	 * Exchange an access code, a refresh token or a username/password for a Token to be POSTed back
	 *
	 * @param grantType
	 * @param code
	 * @param redirectUri
	 * @param clientId
	 * @param secret
	 * @param refreshToken
	 * @param username
	 * @param password
	 *
	 * @return
	 */
	@POST
	@Path("/token")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_JSON})
	public String getToken(@FormParam("grant_type")
	                       @Doc("One of: authorization_code,refresh_token,password,client_credentials (HTTP BASIC)")
			                       String grantType, @FormParam("code")
	                       @Doc("The authorization code from the auth callback (where grant_type is authorization_code)")
			                       String code,
	                       @FormParam("redirect_uri") @Doc("") String redirectUri,
	                       @FormParam("client_id") String clientId,
	                       @FormParam("client_secret") String secret,
	                       @FormParam("refresh_token") @Doc("The refresh token (where grant_type is refresh_token)")
			                       String refreshToken,
	                       @FormParam("username") @Doc("The username (where grant_type is password)") String username,
	                       @FormParam("password") @Doc("The password (where grant_type is password)") String password);


	/**
	 * User Manager extension: given user info, return user record
	 *
	 * @param token
	 * @param clientId if specified, checks that the token is associated with this client (may be required in the future)
	 *
	 * @return
	 */
	@POST
	@Path("/token-to-user-info")
	@Produces(MediaType.APPLICATION_XML)
	UserManagerUser get(@FormParam("token") String token, @FormParam("client_id") String clientId);
}
