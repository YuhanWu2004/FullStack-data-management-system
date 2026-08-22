package com.cicad.app.repository;

import com.cicad.app.entities.Term;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class TermRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public Term get(Integer id) {
		return entityManager.find(Term.class, id);
	}

	public List<Term> getAll() {
		return entityManager
				.createQuery("SELECT t FROM Term t ORDER BY t.startDate DESC, t.name", Term.class)
				.getResultList();
	}

	public Term findCurrent() {
		try {
			return entityManager
					.createQuery("SELECT t FROM Term t WHERE t.current = true", Term.class)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Term create(Term term) {
		entityManager.persist(term);
		return term;
	}

	public Term update(Term term) {
		return entityManager.merge(term);
	}

	public void delete(Integer id) {
		Term term = entityManager.find(Term.class, id);
		if (term != null) {
			entityManager.remove(term);
		}
	}

	/** Flips every term's IS_CURRENT off, so the caller can set exactly one back on. */
	public void clearCurrent() {
		entityManager
				.createQuery("UPDATE Term t SET t.current = false WHERE t.current = true")
				.executeUpdate();
	}
}
