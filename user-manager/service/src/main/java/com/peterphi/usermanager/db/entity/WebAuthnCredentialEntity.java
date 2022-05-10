package com.peterphi.usermanager.db.entity;

import com.peterphi.std.guice.database.annotation.EagerFetch;
import com.peterphi.std.util.HexHelper;
import com.peterphi.usermanager.guice.authentication.webauthn.UUIDUtils;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Arrays;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity(name = "user_account_webauthn_credential")
public class WebAuthnCredentialEntity
{
	/**
	 * Credential ID
	 */
	private String id;

	private String name;
	private UserEntity user;

	private String pkType;
	private String pkTransports;

	private byte[] publicKey;
	private boolean discoverable;

	private long sigCounter;
	private byte[] attestation;
	private byte[] clientDataJson;

	private DateTime created = DateTime.now();
	private DateTime updated;
	private DateTime lastLogin;


	public WebAuthnCredentialEntity()
	{
	}


	public WebAuthnCredentialEntity(UserEntity user,
	                                PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc,
	                                RegistrationResult reg)
	{
		this.setUser(user);

		// Credential ID and transports for allowCredentials
		PublicKeyCredentialDescriptor keyId = reg.getKeyId();
		{
			this.setId(HexHelper.toHex(keyId.getId().getBytes()));

			this.pkType = keyId.getType().name();
			this.pkTransports = keyId
					                    .getTransports()
					                    .stream()
					                    .flatMap(ss -> ss.stream())
					                    .map(AuthenticatorTransport :: getId)
					                    .collect(Collectors.joining(","));
		}

		this.publicKey = reg.getPublicKeyCose().getBytes(); // Public key for verifying authentication signatures
		this.discoverable = reg.isDiscoverable().orElse(false);   // Can this key be used for username-less auth?
		this.sigCounter = reg.getSignatureCount();   // Initial signature counter value
		this.attestation = pkc.getResponse().getAttestationObject().getBytes(); // Store attestation object for future reference
		this.clientDataJson = pkc
				                      .getResponse()
				                      .getClientDataJSON()
				                      .getBytes();     // Store client data for re-verifying signature if needed
	}


	public PublicKeyCredentialDescriptor createPublicKeyCredentialDescriptor()
	{
		final PublicKeyCredentialDescriptor.PublicKeyCredentialDescriptorBuilder builder = PublicKeyCredentialDescriptor
				                                                                                   .builder()
				                                                                                   .id(new ByteArray(HexHelper.fromHex(
						                                                                                   this.id)));

		builder.type(PublicKeyCredentialType.valueOf(this.pkType));

		if (StringUtils.isNotEmpty(this.pkTransports))
			builder.transports(Optional.of(Arrays
					                               .stream(this.pkTransports.split(","))
					                               .map(AuthenticatorTransport :: of)
					                               .collect(Collectors.toCollection(TreeSet :: new))));

		return builder.build();
	}


	public RegisteredCredential createRegisteredCredential()
	{
		return RegisteredCredential
				       .builder()
				       .credentialId(new ByteArray(UUIDUtils.serialise(getId())))
				       .userHandle(new ByteArray(UUIDUtils.serialise(getUser().getWebauthnUserHandle())))
				       .publicKeyCose(new ByteArray(this.publicKey))
				       .signatureCount(this.sigCounter)
				       .build();
	}


	@Id
	@Column(name = "id")
	public String getId()
	{
		return id;
	}


	public void setId(final String id)
	{
		this.id = id;
	}


	@Column(name = "caption")
	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	@EagerFetch
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	public UserEntity getUser()
	{
		return user;
	}


	public void setUser(final UserEntity user)
	{
		this.user = user;
	}


	@Column(name = "pk_type", length = 32)
	public String getPkType()
	{
		return pkType;
	}


	public void setPkType(final String pkType)
	{
		this.pkType = pkType;
	}


	@Column(name = "pk_transports", length = 99, nullable = false)
	public String getPkTransports()
	{
		return pkTransports;
	}


	public void setPkTransports(final String pkTransports)
	{
		this.pkTransports = pkTransports;
	}


	@Column(name = "pk_cose", length = 8192)
	public byte[] getPublicKey()
	{
		return publicKey;
	}


	public void setPublicKey(final byte[] publicKey)
	{
		this.publicKey = publicKey;
	}


	@Column(name = "is_discoverable", nullable = false)
	public boolean isDiscoverable()
	{
		return discoverable;
	}


	public void setDiscoverable(final boolean discoverable)
	{
		this.discoverable = discoverable;
	}


	@Column(name = "sig_counter", nullable = false)
	public long getSigCounter()
	{
		return sigCounter;
	}


	public void setSigCounter(final long sigCounter)
	{
		this.sigCounter = sigCounter;
	}


	@Column(name = "attestation_bytes", length = 8192)
	public byte[] getAttestation()
	{
		return attestation;
	}


	public void setAttestation(final byte[] attestation)
	{
		this.attestation = attestation;
	}


	@Column(name = "client_data_json_bytes", length = 4096)
	public byte[] getClientDataJson()
	{
		return clientDataJson;
	}


	public void setClientDataJson(final byte[] clientDataJson)
	{
		this.clientDataJson = clientDataJson;
	}


	@Column(name = "created_ts", nullable = false)
	public DateTime getCreated()
	{
		return created;
	}


	public void setCreated(final DateTime created)
	{
		this.created = created;
	}


	@Version
	@Column(name = "updated_ts", nullable = false)
	public DateTime getUpdated()
	{
		return updated;
	}


	public void setUpdated(final DateTime updated)
	{
		this.updated = updated;
	}


	@Column(name = "last_login_ts")
	public DateTime getLastLogin()
	{
		return lastLogin;
	}


	public void setLastLogin(final DateTime lastLogin)
	{
		this.lastLogin = lastLogin;
	}
}
