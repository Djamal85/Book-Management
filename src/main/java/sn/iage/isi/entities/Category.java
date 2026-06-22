package sn.iage.isi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "books")
@Builder
@NamedQuery(name = "Category.findAll", query = "SELECT c FROM Category c ORDER BY c.name ASC")
public class Category extends BaseEntity{

    @Column(nullable = false, length = 100)
    private String name;

    private boolean state;

    @OneToMany(mappedBy = "category")
    private List<Book> books;
}
