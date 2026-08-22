package com.cicad.app.repository;

import com.cicad.app.entities.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AppUserRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public AppUser findByUsername(String username) {
		try {
			return entityManager
					.createQuery("SELECT u FROM AppUser u WHERE u.username = :username", AppUser.class)
					.setParameter("username", username)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
