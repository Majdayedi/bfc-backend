package bfc.bfc.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(nullable = false, unique = true)
    private String slug;

    private String category;

    private String author;

    private String publishDate;

    private String readingTime;

    @Column(length = 2000)
    private String summary;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String heroImage;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contentJson;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isTopArticle;
}
