package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.fmi.data.entity.LabelEntity;

public interface LabelEntityRepo extends JpaRepository<LabelEntity, Integer> {

}
