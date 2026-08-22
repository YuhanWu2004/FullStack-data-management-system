package com.cicad.app.service;

import com.cicad.app.entities.Term;
import com.cicad.app.repository.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Terms are a short, staff-managed list (a handful of rows, ever) rather than a growing
 * table like students or courses, so — unlike the other entity services — reads here
 * return a plain list instead of a paginated envelope. A dropdown selector wants every
 * term at once; paginating it would only add ceremony no caller needs.
 */
@Service
@Transactional
public class TermService {

	@Autowired
	private TermRepository termRepository;

	public Term get(Integer id) {
		return termRepository.get(id);
	}

	public List<Term> getAll() {
		return termRepository.getAll();
	}

	public Term getCurrent() {
		return termRepository.findCurrent();
	}

	public Term create(Term sourceTerm) {
		if (sourceTerm.getName() == null || sourceTerm.getName().isBlank()) {
			throw new RuntimeException("Term name is required");
		}
		Term term = new Term();
		term.setName(sourceTerm.getName());
		term.setStartDate(sourceTerm.getStartDate());
		term.setEndDate(sourceTerm.getEndDate());
		return termRepository.create(term);
	}

	public Term update(Term sourceTerm) {
		Term existing = termRepository.get(sourceTerm.getId());
		if (existing == null) {
			throw new RuntimeException("Term not found with id: " + sourceTerm.getId());
		}
		if (sourceTerm.getName() != null) {
			existing.setName(sourceTerm.getName());
		}
		if (sourceTerm.getStartDate() != null) {
			existing.setStartDate(sourceTerm.getStartDate());
		}
		if (sourceTerm.getEndDate() != null) {
			existing.setEndDate(sourceTerm.getEndDate());
		}
		return termRepository.update(existing);
	}

	public void delete(Integer id) {
		Term term = termRepository.get(id);
		if (term == null) {
			return;
		}
		if (term.isCurrent()) {
			throw new RuntimeException("Cannot delete the current term — set a different term as current first");
		}
		termRepository.delete(id);
	}

	/** Exactly one term is current at a time; this is what enforces that. */
	public Term setCurrent(Integer id) {
		Term term = termRepository.get(id);
		if (term == null) {
			throw new RuntimeException("Term not found with id: " + id);
		}
		termRepository.clearCurrent();
		term.setCurrent(true);
		return termRepository.update(term);
	}
}
