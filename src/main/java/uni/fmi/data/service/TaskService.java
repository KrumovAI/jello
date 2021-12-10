package uni.fmi.data.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import uni.fmi.data.entity.TaskEntity;
import uni.fmi.data.repo.TaskEntityRepo;

import java.util.Collection;

@Service
public class TaskService extends CrudService<TaskEntity, Integer> {

	private TaskEntityRepo repository;

	public TaskService(@Autowired TaskEntityRepo repository) {
		this.repository = repository;
	}

	@Override
	protected TaskEntityRepo getRepository() {
		return repository;
	}

	public Collection<TaskEntity> findAll() {
		return repository.findAll();
	}

	public int count(String filter) {
		return (int) repository.findAll().stream().filter(u->StringUtils.containsIgnoreCase(u.getName(), filter)).count();
	}

}
