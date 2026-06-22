package sn.iage.isi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Book extends BaseEntity{

    @Column(nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(name = "publication_year",  nullable = false)
    private int publicationYear;

    @Column(nullable = false, name = "count_pages")
    private int countPages;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
