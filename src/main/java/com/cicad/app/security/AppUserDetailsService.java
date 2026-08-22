package com.cicad.app.security;

import com.cicad.app.entities.AppUser;
import com.cicad.app.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

	@Autowired
	private AppUserRepository appUserRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser user = appUserRepository.findByUsername(username);
		if (user == null) {
			// Deliberately vague: the message reaches the login form, and naming which
			// half was wrong tells an attacker which usernames exist.
			throw new UsernameNotFoundException("Bad credentials");
		}
		return new AppUserDetails(user);
	}
}
