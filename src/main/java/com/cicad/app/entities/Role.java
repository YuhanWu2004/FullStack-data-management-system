package com.cicad.app.entities;

import jakarta.persistence.*;

/**
 * A grantable authority. NAME is stored in Spring Security's own convention
 * ("ROLE_STAFF"), so it can be handed to SimpleGrantedAuthority unchanged.
 */
@Entity
@Table(name = "ROLES")
public class Role {

	@Id
	@Column(name = "ID", nullable = false, unique = true, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "NAME", nullable = false, unique = true)
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
