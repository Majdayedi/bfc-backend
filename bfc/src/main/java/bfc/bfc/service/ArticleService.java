package bfc.bfc.service;

import bfc.bfc.dto.ArticleRequest;
import bfc.bfc.dto.ArticleResponse;
import bfc.bfc.entities.Article;
import bfc.bfc.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public List<ArticleResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toArticleResponse)
                .collect(Collectors.toList());
    }

    public List<ArticleResponse> getTopArticles() {
        return repository.findTopArticles()
                .stream()
                .map(this::toArticleResponse)
                .collect(Collectors.toList());
    }

    public ArticleResponse getById(Long id) {
        Article article = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));
        return toArticleResponse(article);
    }

    public ArticleResponse getBySlug(String slug) {
        Article article = repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Article not found with slug: " + slug));
        return toArticleResponse(article);
    }

    @Transactional
    public ArticleResponse create(ArticleRequest request) {
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = sanitizeSlug(request.getTitle());
        }

        // Ensure slug uniqueness
        int count = 1;
        String baseSlug = slug;
        while (repository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }

        Article article = toEntity(request);
        article.setSlug(slug);
        return toArticleResponse(repository.save(article));
    }

    @Transactional
    public ArticleResponse update(Long id, ArticleRequest request) {
        Article existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));

        existing.setTitle(request.getTitle());
        existing.setSubtitle(request.getSubtitle());
        existing.setCategory(request.getCategory());
        existing.setAuthor(request.getAuthor());
        existing.setPublishDate(request.getPublishDate());
        existing.setReadingTime(request.getReadingTime());
        existing.setSummary(request.getSummary());
        existing.setHeroImage(request.getHeroImage());
        existing.setContentJson(request.getContentJson());
        existing.setTopArticle(request.getIsTopArticle() != null && request.getIsTopArticle());

        // Update slug if a new unique one is requested
        String newSlug = request.getSlug();
        if (newSlug != null && !newSlug.trim().isEmpty() && !newSlug.equals(existing.getSlug())) {
            int count = 1;
            String baseSlug = newSlug;
            while (repository.existsBySlug(newSlug)) {
                newSlug = baseSlug + "-" + count;
                count++;
            }
            existing.setSlug(newSlug);
        }

        return toArticleResponse(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Article not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private String sanitizeSlug(String title) {
        return (title != null ? title : "article")
                .toLowerCase()
                .replaceAll("[^a-z0-9_-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public ArticleResponse toArticleResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .slug(article.getSlug())
                .category(article.getCategory())
                .author(article.getAuthor())
                .publishDate(article.getPublishDate())
                .readingTime(article.getReadingTime())
                .summary(article.getSummary())
                .heroImage(article.getHeroImage())
                .contentJson(article.getContentJson())
                .isTopArticle(article.isTopArticle())
                .build();
    }

    private Article toEntity(ArticleRequest request) {
        return Article.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .slug(request.getSlug())
                .category(request.getCategory())
                .author(request.getAuthor())
                .publishDate(request.getPublishDate())
                .readingTime(request.getReadingTime())
                .summary(request.getSummary())
                .heroImage(request.getHeroImage())
                .contentJson(request.getContentJson())
                .isTopArticle(request.getIsTopArticle() != null && request.getIsTopArticle())
                .build();
    }
}
