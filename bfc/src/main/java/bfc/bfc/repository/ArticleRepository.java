package bfc.bfc.repository;

import bfc.bfc.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);
    boolean existsBySlug(String slug);
    
    @Query(value = "SELECT * FROM articles WHERE is_top_article = true", nativeQuery = true)
    List<Article> findTopArticles();
}
