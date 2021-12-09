package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import uni.fmi.data.entity.UserEntity;

public interface UserRepo extends JpaRepository<UserEntity, Integer>{

}
