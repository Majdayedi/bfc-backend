package bfc.bfc.service;

import bfc.bfc.dto.ArticleResponse;
import bfc.bfc.dto.ProjectRequest;
import bfc.bfc.dto.ProjectResponse;
import bfc.bfc.entities.Article;
import bfc.bfc.entities.Project;
import bfc.bfc.repository.ArticleRepository;
import bfc.bfc.repository.ProjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository repository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProjectService(ProjectRepository repository, ArticleRepository articleRepository, ArticleService articleService) {
        this.repository = repository;
        this.articleRepository = articleRepository;
        this.articleService = articleService;
    }

    public List<ProjectResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getById(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project project = toEntity(request);
        return toResponse(repository.save(project));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        existing.setTitle(request.getTitle());
        existing.setCategory(request.getCategory());
        existing.setClient(request.getClient());
        existing.setCountry(request.getCountry());
        existing.setRepresentativeSlug(request.getRepresentativeSlug());
        existing.setFlag(request.getFlag());
        existing.setYear(request.getYear());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
        existing.setDescription(request.getDescription());
        existing.setAccent(request.getAccent());
        existing.setImageUrl(request.getImageUrl());
        existing.setClientImageUrl(request.getClientImageUrl());
        existing.setContentJson(request.getContentJson());

        return toResponse(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private List<ArticleResponse> fetchRelatedArticles(String contentJson) {
        List<ArticleResponse> related = new ArrayList<>();
        if (contentJson == null || contentJson.trim().isEmpty()) {
            return related;
        }
        try {
            Map<String, Object> data = mapper.readValue(contentJson, new TypeReference<Map<String, Object>>() {});
            if (data.containsKey("relatedArticleIds")) {
                Object idsObj = data.get("relatedArticleIds");
                if (idsObj instanceof List) {
                    List<?> idsList = (List<?>) idsObj;
                    for (Object idObj : idsList) {
                        Long artId = null;
                        if (idObj instanceof Number) {
                            artId = ((Number) idObj).longValue();
                        } else if (idObj instanceof String) {
                            try {
                                artId = Long.parseLong((String) idObj);
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                        if (artId != null) {
                            // Use repository directly to avoid transaction rollback issues
                            Optional<Article> artOpt = articleRepository.findById(artId);
                            artOpt.ifPresent(article -> related.add(articleService.toArticleResponse(article)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // return empty list on any parse error
        }
        return related;
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .category(project.getCategory())
                .client(project.getClient())
                .country(project.getCountry())
                .representativeSlug(project.getRepresentativeSlug())
                .flag(project.getFlag())
                .year(project.getYear())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .description(project.getDescription())
                .accent(project.getAccent())
                .imageUrl(project.getImageUrl())
                .clientImageUrl(project.getClientImageUrl())
                .contentJson(project.getContentJson())
                .relatedArticles(fetchRelatedArticles(project.getContentJson()))
                .build();
    }

    private Project toEntity(ProjectRequest request) {
        String year = request.getYear();
        if (year == null || year.trim().isEmpty()) {
            if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
                year = request.getStartDate().trim() + (request.getEndDate() != null && !request.getEndDate().trim().isEmpty() ? " - " + request.getEndDate().trim() : "");
            }
        }
        return Project.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .client(request.getClient())
                .country(request.getCountry())
                .representativeSlug(request.getRepresentativeSlug())
                .flag(request.getFlag())
                .year(year)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .accent(request.getAccent())
                .imageUrl(request.getImageUrl())
                .clientImageUrl(request.getClientImageUrl())
                .contentJson(request.getContentJson())
                .build();
    }
}
