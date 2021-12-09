package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.fmi.data.entity.TaskListEntity;

public interface TaskListEntityRepo extends JpaRepository<TaskListEntity, Integer> {

}
