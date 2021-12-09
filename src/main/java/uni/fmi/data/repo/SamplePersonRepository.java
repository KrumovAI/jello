package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.fmi.data.entity.SamplePerson;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, Integer> {

}