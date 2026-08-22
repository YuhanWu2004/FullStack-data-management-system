package com.cicad.app.security;

import com.cicad.app.entities.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The authenticated principal. Carries the linked STUDENT_ID / PROFESSOR_ID so ownership
 * checks can compare against the server's own record instead of a value the client sent.
 */
public class AppUserDetails implements UserDetails {

	private final Integer userId;
	private final String username;
	private final String passwordHash;
	private final String displayName;
	private final boolean enabled;
	private final Integer studentId;
	private final Integer professorId;
	private final List<GrantedAuthority> authorities;

	public AppUserDetails(AppUser user) {
		this.userId = user.getId();
		this.username = user.getUsername();
		this.passwordHash = user.getPasswordHash();
		this.displayName = user.getDisplayName();
		this.enabled = user.isEnabled();
		this.studentId = user.getStudent() == null ? null : user.getStudent().getId();
		this.professorId = user.getProfessor() == null ? null : user.getProfessor().getId();
		this.authorities = user.getRoles().stream()
				.map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.getName()))
				.toList();
	}

	public Integer getUserId() {
		return userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public Integer getProfessorId() {
		return professorId;
	}

	public List<String> getRoleNames() {
		return authorities.stream().map(GrantedAuthority::getAuthority).toList();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
