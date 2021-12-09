package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import uni.fmi.data.entity.NotificationEntity;

public interface NotificationEntityRepo extends JpaRepository<NotificationEntity, Integer> {

}
