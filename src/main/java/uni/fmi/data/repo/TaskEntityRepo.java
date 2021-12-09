package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.fmi.data.entity.TaskEntity;

public interface TaskEntityRepo extends JpaRepository<TaskEntity, Integer> {

}
