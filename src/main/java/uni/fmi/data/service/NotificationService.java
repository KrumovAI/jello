package uni.fmi.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import uni.fmi.data.entity.NotificationEntity;
import uni.fmi.data.repo.NotificationEntityRepo;

@Service
public class NotificationService extends CrudService<NotificationEntity, Integer> {

	private NotificationEntityRepo repository;

	public NotificationService(@Autowired NotificationEntityRepo repository) {
		this.repository = repository;
	}

	@Override
	protected NotificationEntityRepo getRepository() {
		return repository;
	}

}
