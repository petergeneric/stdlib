package com.peterphi.std.guice.hibernate.webquery;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
class ParentEntity
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "obj_name")
	private String name;

	@Column(name = "deprecated")
	private boolean deprecated = false;

	@Column(name = "someBytes")
	@Lob
	private byte[] someBytes = "some bytes".getBytes();

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "other_object_id", nullable = true)
	private ChildEntity otherObject;

	@OneToMany(mappedBy = "parent")
	private Set<ChildEntity> children;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "parent_friends", joinColumns = @JoinColumn(name = "parent_id"))
	private Set<HumanEmbeddedEntity> friends = new HashSet<>();


	public Long getId()
	{
		return id;
	}


	public void setId(final Long id)
	{
		this.id = id;
	}


	public String getName()
	{
		return name;
	}


	public void setName(final String name)
	{
		this.name = name;
	}


	boolean isDeprecated()
	{
		return deprecated;
	}


	void setDeprecated(final boolean deprecated)
	{
		this.deprecated = deprecated;
	}


	public byte[] getSomeBytes()
	{
		return someBytes;
	}


	public void setSomeBytes(final byte[] someBytes)
	{
		this.someBytes = someBytes;
	}


	public ChildEntity getOtherObject()
	{
		return otherObject;
	}


	public void setOtherObject(final ChildEntity otherObject)
	{
		this.otherObject = otherObject;
	}


	public Set<ChildEntity> getChildren()
	{
		return children;
	}


	public void setChildren(final Set<ChildEntity> children)
	{
		this.children = children;
	}


	public Set<HumanEmbeddedEntity> getFriends()
	{
		return friends;
	}


	public void setFriends(final Set<HumanEmbeddedEntity> friends)
	{
		this.friends = friends;
	}
}
