package bfc.bfc.service;

import bfc.bfc.dto.ServicePageRequest;
import bfc.bfc.dto.ServicePageResponse;
import bfc.bfc.entities.ServicePage;
import bfc.bfc.entities.ServiceCard;
import bfc.bfc.repositories.ServicePageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePageService {

    private final ServicePageRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ServicePageResponse> getAllServicePages() {
        return repository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServicePageResponse getServicePageBySlug(String slug) {
        ServicePage servicePage = repository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service page not found"));
        return mapToResponse(servicePage);
    }

    public ServicePageResponse getServicePageById(Long id) {
        ServicePage servicePage = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service page not found"));
        return mapToResponse(servicePage);
    }

    @Transactional
    public ServicePageResponse createServicePage(ServicePageRequest request) {
        if (repository.findBySlug(request.getSlug()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A service with this slug already exists");
        }
        ServicePage servicePage = ServicePage.builder()
                .slug(request.getSlug())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .layoutType(request.getLayoutType())
                .contentJson(request.getContentJson())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        saveCardsFromContentJson(servicePage);
        ServicePage saved = repository.save(servicePage);
        return mapToResponse(saved);
    }

    @Transactional
    public ServicePageResponse updateServicePage(Long id, ServicePageRequest request) {
        ServicePage servicePage = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service page not found"));

        if (!servicePage.getSlug().equals(request.getSlug()) && repository.findBySlug(request.getSlug()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A service with this slug already exists");
        }

        servicePage.setSlug(request.getSlug());
        servicePage.setTitle(request.getTitle());
        servicePage.setSubtitle(request.getSubtitle());
        servicePage.setDescription(request.getDescription());
        servicePage.setLayoutType(request.getLayoutType());
        servicePage.setContentJson(request.getContentJson());
        if (request.getDisplayOrder() != null) {
            servicePage.setDisplayOrder(request.getDisplayOrder());
        }

        saveCardsFromContentJson(servicePage);
        ServicePage updated = repository.save(servicePage);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteServicePage(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void reorderServicePages(List<? extends Number> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i).longValue();
            final int orderIndex = i;
            repository.findById(id).ifPresent(sp -> {
                sp.setDisplayOrder(orderIndex);
                repository.save(sp);
            });
        }
    }

    private void saveCardsFromContentJson(ServicePage servicePage) {
        if (servicePage.getCards() == null) {
            servicePage.setCards(new ArrayList<>());
        } else {
            servicePage.getCards().clear();
        }
        
        String json = servicePage.getContentJson();
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        
        try {
            Map<String, Object> contentMap = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            if (contentMap.containsKey("categories")) {
                List<Map<String, Object>> categories = (List<Map<String, Object>>) contentMap.get("categories");
                if (categories != null) {
                    for (Map<String, Object> cat : categories) {
                        String categoryName = (String) cat.get("name");
                        List<Map<String, Object>> boxes = (List<Map<String, Object>>) cat.get("boxes");
                        if (boxes != null) {
                            for (Map<String, Object> box : boxes) {
                                ServiceCard card = ServiceCard.builder()
                                        .title((String) box.get("title"))
                                        .categoryName(categoryName)
                                        .items((List<String>) box.get("items"))
                                        .servicePage(servicePage)
                                        .build();
                                servicePage.getCards().add(card);
                            }
                        }
                    }
                }
            } else if (contentMap.containsKey("boxes")) {
                List<Map<String, Object>> boxes = (List<Map<String, Object>>) contentMap.get("boxes");
                if (boxes != null) {
                    for (Map<String, Object> box : boxes) {
                        ServiceCard card = ServiceCard.builder()
                                .title((String) box.get("title"))
                                .categoryName(null)
                                .items((List<String>) box.get("items"))
                                .servicePage(servicePage)
                                .build();
                        servicePage.getCards().add(card);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateContentJsonFromCards(ServicePage servicePage) {
        List<ServiceCard> cards = servicePage.getCards();
        if (cards == null || cards.isEmpty()) {
            return;
        }
        
        try {
            boolean hasCategories = false;
            for (ServiceCard card : cards) {
                if (card.getCategoryName() != null && !card.getCategoryName().trim().isEmpty()) {
                    hasCategories = true;
                    break;
                }
            }
            
            Map<String, Object> contentMap = new java.util.HashMap<>();
            if (hasCategories) {
                Map<String, List<Map<String, Object>>> grouped = new java.util.LinkedHashMap<>();
                for (ServiceCard card : cards) {
                    String catName = card.getCategoryName() != null ? card.getCategoryName() : "General";
                    grouped.computeIfAbsent(catName, k -> new ArrayList<>());
                    
                    Map<String, Object> boxMap = new java.util.HashMap<>();
                    boxMap.put("id", String.format("%02d", grouped.get(catName).size() + 1));
                    boxMap.put("title", card.getTitle() != null ? card.getTitle() : "");
                    boxMap.put("items", card.getItems() != null ? card.getItems() : new ArrayList<>());
                    boxMap.put("image", "");
                    grouped.get(catName).add(boxMap);
                }
                
                List<Map<String, Object>> categoriesList = new ArrayList<>();
                for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
                    Map<String, Object> catMap = new java.util.HashMap<>();
                    catMap.put("name", entry.getKey());
                    catMap.put("boxes", entry.getValue());
                    categoriesList.add(catMap);
                }
                contentMap.put("categories", categoriesList);
            } else {
                List<Map<String, Object>> boxesList = new ArrayList<>();
                for (ServiceCard card : cards) {
                    Map<String, Object> boxMap = new java.util.HashMap<>();
                    boxMap.put("id", String.format("%02d", boxesList.size() + 1));
                    boxMap.put("title", card.getTitle() != null ? card.getTitle() : "");
                    boxMap.put("items", card.getItems() != null ? card.getItems() : new ArrayList<>());
                    boxMap.put("image", "");
                    boxesList.add(boxMap);
                }
                contentMap.put("boxes", boxesList);
            }
            
            servicePage.setContentJson(objectMapper.writeValueAsString(contentMap));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServicePageResponse mapToResponse(ServicePage servicePage) {
        populateContentJsonFromCards(servicePage);
        return ServicePageResponse.builder()
                .id(servicePage.getId())
                .slug(servicePage.getSlug())
                .title(servicePage.getTitle())
                .subtitle(servicePage.getSubtitle())
                .description(servicePage.getDescription())
                .layoutType(servicePage.getLayoutType())
                .contentJson(servicePage.getContentJson())
                .displayOrder(servicePage.getDisplayOrder())
                .build();
    }
}
