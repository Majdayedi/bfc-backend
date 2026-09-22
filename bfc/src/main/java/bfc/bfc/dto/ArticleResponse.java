package bfc.bfc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String slug;
    private String category;
    private String author;
    private String publishDate;
    private String readingTime;
    private String summary;
    private String heroImage;
    private String contentJson;
    private boolean isTopArticle;
}
