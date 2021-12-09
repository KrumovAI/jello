package uni.fmi.data.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import uni.fmi.data.entity.TaskEntity;
import uni.fmi.data.repo.TaskEntityRepo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

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

	public void saveAll(List<TaskEntity> users) {
		repository.saveAll(users);
	}

	public Stream<TaskEntity> fetchItems(String filter, int offset, int limit) {
		return repository.findAll().stream().filter(u-> StringUtils.containsIgnoreCase(u.getName(), filter));
	}

	public int count(String filter) {
		return (int) repository.findAll().stream().filter(u->StringUtils.containsIgnoreCase(u.getName(), filter)).count();
	}

}
