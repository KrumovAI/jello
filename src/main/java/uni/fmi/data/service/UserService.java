package uni.fmi.data.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import uni.fmi.data.entity.UserEntity;
import uni.fmi.data.repo.UserRepo;

@Service
public class UserService extends CrudService<UserEntity, Integer> {

	private UserRepo repository;

	public UserService(@Autowired UserRepo repository) {
		this.repository = repository;
	}

	@Override
	protected UserRepo getRepository() {
		return repository;
	}

	public Collection<UserEntity> findAll() {
		return repository.findAll();
	}

	public void saveAll(List<UserEntity> users) {
		repository.saveAll(users);
	}

	public Stream<UserEntity> fetchItems(String filter, int offset, int limit) {
		return repository.findAll().stream().filter(u->StringUtils.containsIgnoreCase(u.getUsername(), filter));
	}

	public int count(String filter) {
		return (int) repository.findAll().stream().filter(u->StringUtils.containsIgnoreCase(u.getUsername(), filter)).count();
	}

}
