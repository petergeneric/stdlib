package com.peterphi.usermanager.guice.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.peterphi.std.guice.common.GuiceModule;
import com.peterphi.usermanager.guice.authentication.webauthn.WebauthCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;

public class WebAuthnModule extends GuiceModule
{
	@Override
	protected void configure()
	{
	}


	@Provides
	@Singleton
	public RelyingParty createRelyingParty(WebauthCredentialRepository repo)
	{
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity
				                                  .builder()
				                                  .id("example.org")  // Set this to a parent domain that covers all subdomains
				                                  .name("User Manager")
				                                  .build();


		return RelyingParty.builder().identity(rpIdentity).credentialRepository(repo).build();
	}
}
