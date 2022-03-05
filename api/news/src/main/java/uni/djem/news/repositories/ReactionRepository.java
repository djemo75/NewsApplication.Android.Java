package uni.djem.news.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.news.entities.ReactionEntity;

@Repository
public interface ReactionRepository extends JpaRepository<ReactionEntity, Integer> {
	ReactionEntity findById(int id);
	ReactionEntity findByNewsIdAndUserId(int newsId, int userId);
	List<ReactionEntity> findByNewsIdOrderByCreatedDateDesc(int id);
}