package uni.fmi.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import uni.fmi.data.entity.CommentEntity;
import uni.fmi.data.repo.CommentEntityRepo;

@Service
public class CommentService extends CrudService<CommentEntity, Integer> {

	private CommentEntityRepo repository;

	public CommentService(@Autowired CommentEntityRepo repository) {
		this.repository = repository;
	}

	@Override
	protected CommentEntityRepo getRepository() {
		return repository;
	}

}
