package uni.fmi.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import uni.fmi.data.entity.CommentEntity;

public interface CommentEntityRepo extends JpaRepository<CommentEntity, Integer> {

}
