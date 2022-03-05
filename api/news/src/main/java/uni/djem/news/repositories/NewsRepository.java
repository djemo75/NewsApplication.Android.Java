package uni.djem.news.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uni.djem.news.entities.NewsEntity;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntity, Integer> {
	NewsEntity findById(int id);
	List<NewsEntity> findByTitleContainingIgnoreCaseOrderByCreatedDateDesc(String title);
	List<NewsEntity> findByUserUsernameContaining(String username);
}