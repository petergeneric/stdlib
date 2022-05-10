package com.peterphi.usermanager.guice.authentication.webauthn;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.peterphi.std.guice.common.auth.iface.CurrentUser;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.WebAuthnCredentialDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.db.entity.WebAuthnCredentialEntity;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public class WebAuthnAuthenticationService
{
	@Inject
	RelyingParty party;

	@Inject
	WebAuthnCredentialDaoImpl credentialDao;

	@Inject
	UserDaoImpl userDao;

	@Inject
	Provider<CurrentUser> userProvider;


	@Transactional
	public PublicKeyCredentialCreationOptions startRegister(final String username)
	{
		assert username.equals(userProvider.get().getUsername());

		ByteArray handle = new ByteArray(UUIDUtils.serialise(userDao.getOrAssignHandle(username)));

		final UserEntity user = userDao.getUserByEmail(username);

		final StartRegistrationOptions.StartRegistrationOptionsBuilder builder = StartRegistrationOptions
				                                                                         .builder()
				                                                                         .user(UserIdentity
						                                                                               .builder()
						                                                                               .name(user.getEmail())
						                                                                               .displayName(user.getName())
						                                                                               .id(handle)
						                                                                               .build())
				                                                                         .authenticatorSelection(
						                                                                         AuthenticatorSelectionCriteria
								                                                                         .builder()
								                                                                         .residentKey(
										                                                                         ResidentKeyRequirement.PREFERRED)
								                                                                         .build());
		return party.startRegistration(builder.build());
	}


	@Transactional
	public WebAuthnCredentialEntity finishRegister(final String username,
	                                               PublicKeyCredentialCreationOptions request,
	                                               final String json)
	{
		assert username.equals(userProvider.get().getUsername());

		final UserEntity user = userDao.getUserByEmail(username);

		final PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc;
		final RegistrationResult reg;
		try
		{
			pkc = PublicKeyCredential.parseRegistrationResponseJson(json);

			reg = party.finishRegistration(FinishRegistrationOptions.builder().request(request).response(pkc).build());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Registration failed! ", e);
		}

		WebAuthnCredentialEntity c = new WebAuthnCredentialEntity(user, pkc, reg);

		credentialDao.save(c);

		return c;
	}


	@Transactional
	public AssertionRequest startLogin(final String username)
	{
		final StartAssertionOptions.StartAssertionOptionsBuilder builder = StartAssertionOptions.builder();

		if (StringUtils.isNotEmpty(username))
			builder.username(username);

		return party.startAssertion(builder.build());
	}


	@Transactional
	public WebAuthnCredentialEntity login(AssertionRequest req, final String assertionResponseJson)
	{
		try
		{
			final PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> assertionResponse = PublicKeyCredential.parseAssertionResponseJson(
					assertionResponseJson);
			final AssertionResult result = party.finishAssertion(FinishAssertionOptions
					                                                     .builder()
					                                                     .request(req)
					                                                     .response(assertionResponse)
					                                                     .build());

			if (result.isSuccess())
			{
				final WebAuthnCredentialEntity cred = credentialDao.getById(UUIDUtils
						                                                            .parse(result.getCredentialId().getBytes())
						                                                            .toString());

				if (cred == null)
					throw new RuntimeException("Unable to retrieve Credential for ID!");

				cred.setSigCounter(result.getSignatureCount());
				cred.setLastLogin(DateTime.now());

				credentialDao.update(cred);

				return cred;
			}
			else
			{
				throw new RuntimeException("Login via WebAuthn failed: " + result.getWarnings());
			}
		}
		catch (Throwable e)
		{
			throw new RuntimeException("WebAuthn login failed! " + e.getMessage(), e);
		}
	}
}
