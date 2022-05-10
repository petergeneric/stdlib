package com.peterphi.usermanager.guice.authentication.webauthn;

import com.google.inject.Inject;
import com.peterphi.std.guice.database.annotation.Transactional;
import com.peterphi.std.guice.restclient.jaxb.webquery.WebQuery;
import com.peterphi.std.util.HexHelper;
import com.peterphi.usermanager.db.dao.hibernate.UserDaoImpl;
import com.peterphi.usermanager.db.dao.hibernate.WebAuthnCredentialDaoImpl;
import com.peterphi.usermanager.db.entity.UserEntity;
import com.peterphi.usermanager.db.entity.WebAuthnCredentialEntity;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WebauthCredentialRepository implements CredentialRepository
{
	@Inject
	UserDaoImpl userDao;

	@Inject
	WebAuthnCredentialDaoImpl dao;


	@Override
	@Transactional(readOnly = true)
	public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username)
	{
		final UserEntity user = userDao.getUserByEmail(username);

		final List<WebAuthnCredentialEntity> creds = dao.find(new WebQuery().eq("user:id", user.getId())).getList();

		return creds.stream().map(c -> c.createPublicKeyCredentialDescriptor()).collect(Collectors.toSet());
	}


	@Override
	public Optional<ByteArray> getUserHandleForUsername(final String username)
	{
		final String handle = userDao.getOrAssignHandle(username);

		if (handle != null)
			return Optional.of(new ByteArray(UUIDUtils.serialise(handle)));
		else
			return Optional.empty();
	}


	@Override
	public Optional<String> getUsernameForUserHandle(final ByteArray userHandle)
	{
		final UserEntity entity = userDao.getByHandle(UUIDUtils.parse(userHandle.getBytes()).toString());

		if (entity != null)
			return Optional.of(entity.getEmail());
		else
			return Optional.empty();
	}


	@Override
	public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle)
	{
		final WebQuery q = new WebQuery();

		// optionally constrain by user
		if (userHandle != null)
		{
			final Optional<String> username = getUsernameForUserHandle(userHandle);

			if (username.isEmpty())
				return Optional.empty();

			final UserEntity user = userDao.getUserByEmail(username.get());

			if (user == null)
				return Optional.empty();

			q.eq("user:id", user.getId());
		}

		final WebAuthnCredentialEntity cred = dao
				                                      .find(q.eq("id", HexHelper.toHex(credentialId.getBytes())).limit(1))
				                                      .uniqueResult();

		return Optional.ofNullable(cred).map(c -> c.createRegisteredCredential());
	}


	@Override
	public Set<RegisteredCredential> lookupAll(final ByteArray credentialId)
	{
		return lookup(credentialId, null).map(Collections :: singleton).orElse(Collections.emptySet());
	}
}
