package uni.fmi.data.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.repo.LabelEntityRepo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LabelService extends CrudService<LabelEntity, Integer> {

	private LabelEntityRepo repository;

	public LabelService(@Autowired LabelEntityRepo repository) {
		this.repository = repository;
	}

	@Override
	protected LabelEntityRepo getRepository() {
		return repository;
	}


	public Collection<LabelEntity> findAll() {
		return repository.findAll();
	}

	public void saveAll(List<LabelEntity> users) {
		repository.saveAll(users);
	}

	public Stream<LabelEntity> fetchItems(String filter, int offset, int limit) {
		return repository.findAll().stream().filter(u-> StringUtils.containsIgnoreCase(u.getName(), filter));
	}

	public int count(String filter) {
		return (int) repository.findAll().stream().filter(u->StringUtils.containsIgnoreCase(u.getName(), filter)).count();
	}

}
