package sn.iage.isi.main;

import sn.iage.isi.entities.Book;
import sn.iage.isi.entities.Category;
import sn.iage.isi.repositories.BookRepository;
import sn.iage.isi.repositories.CategoryRepository;
import sn.iage.isi.repositories.JpaUtil;

import java.util.List;

public class Main {

    static CategoryRepository categoryRepo = new CategoryRepository();
    static BookRepository bookRepo = new BookRepository();

    public static void main(String[] args) {

        // ── 1. Catégories (sans doublons) ────────────────────────────────────
        System.out.println("=== Insertion des catégories ===");
        Category roman        = getOrCreateCategory("Roman");
        Category informatique = getOrCreateCategory("Informatique");
        Category nouvelles    = getOrCreateCategory("Nouvelles");

        // ── 2. Livres (sans doublons, ISBN généré automatiquement) ───────────
        System.out.println("\n=== Insertion des livres ===");
        getOrCreateBook("L'Étranger",              "Albert Camus",              1942, 186, roman);
        getOrCreateBook("Le Petit Prince",          "Antoine de Saint-Exupéry", 1943,  96, roman);
        getOrCreateBook("Clean Code",               "Robert C. Martin",          2008, 431, informatique);
        getOrCreateBook("Java : Les fondamentaux",  "Kathy Sierra",              2005, 688, informatique);
        getOrCreateBook("Nouvelles du monde",        "Guy de Maupassant",         1885, 224, nouvelles);

        // ── 3. ListAllBooks() ─────────────────────────────────────────────────
        System.out.println("\n=== ListAllBooks() ===");
        for (Book b : bookRepo.ListAllBooks()) {
            System.out.printf("  [%d] %-35s | %-25s | %d | %d pages | %s%n",
                    b.getId(), b.getTitle(), b.getAuthor(),
                    b.getPublicationYear(), b.getCountPages(),
                    b.getCategory().getName());
        }

        // ── 4. findBookById() ────────────────────────────────────────────────
        System.out.println("\n=== findBookById(1) ===");
        try {
            Book b = bookRepo.findBookById(1);
            System.out.println("  Trouvé : " + b.getTitle() + " — " + b.getAuthor());
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        // ── 5. findBookByIsbn() ──────────────────────────────────────────────
        System.out.println("\n=== findBookByIsbn() du premier livre ===");
        Book premier = bookRepo.ListAllBooks().get(0);
        Book trouve  = bookRepo.findBookByIsbn(premier.getIsbn());
        System.out.println("  Recherche ISBN : " + premier.getIsbn());
        System.out.println("  Résultat       : " + (trouve != null ? trouve.getTitle() : "non trouvé"));

        // ── 6. ListeBooksByCategory() ─────────────────────────────────────────
        System.out.println("\n=== ListeBooksByCategory('Informatique') ===");
        for (Book b : bookRepo.ListeBooksByCategory("Informatique")) {
            System.out.println("  => " + b.getTitle());
        }

        // ── 7. searchBooksByTitle() ───────────────────────────────────────────
        System.out.println("\n=== searchBooksByTitle('java') ===");
        for (Book b : bookRepo.searchBooksByTitle("java")) {
            System.out.println("  => " + b.getTitle() + " par " + b.getAuthor());
        }

        // ── 8. searchBooksByAuthor() ──────────────────────────────────────────
        System.out.println("\n=== searchBooksByAuthor('camus') ===");
        for (Book b : bookRepo.searchBooksByAuthor("camus")) {
            System.out.println("  => " + b.getTitle() + " par " + b.getAuthor());
        }

        // ── 9. searchBooksAfterYear() ─────────────────────────────────────────
        System.out.println("\n=== searchBooksAfterYear(2000) ===");
        for (Book b : bookRepo.searchBooksAfterYear(2000)) {
            System.out.printf("  => %s (%d)%n", b.getTitle(), b.getPublicationYear());
        }

        // ── 10. updateBook() ──────────────────────────────────────────────────
        System.out.println("\n=== updateBook() — modification du premier livre ===");
        Book aModifier = bookRepo.ListAllBooks().get(0);
        System.out.println("  Avant : " + aModifier.getTitle() + " (" + aModifier.getPublicationYear() + ")");
        Book modifie = bookRepo.updateBook(aModifier.getId(), Book.builder()
                .title(aModifier.getTitle() + " [MàJ]")
                .author(aModifier.getAuthor())
                .publicationYear(aModifier.getPublicationYear())
                .countPages(aModifier.getCountPages())
                .category(aModifier.getCategory())
                .build());
        System.out.println("  Après : " + modifie.getTitle());

        // ── 11. deleteBook() ──────────────────────────────────────────────────
        System.out.println("\n=== deleteBook() — suppression du livre modifié ===");
        bookRepo.deleteBook(modifie.getId());
        System.out.println("  Livre [" + modifie.getId() + "] supprimé.");
        System.out.println("  Total après suppression : " + bookRepo.countAllBooks() + " livre(s)");

        // ── 12. countBooksByCategory() ────────────────────────────────────────
        System.out.println("\n=== countBooksByCategory() ===");
        for (Object[] row : bookRepo.countBooksByCategory()) {
            System.out.printf("  %-20s : %d livre(s)%n", row[0], row[1]);
        }

        // ── 13. countAllBooks() ───────────────────────────────────────────────
        System.out.println("\n=== countAllBooks() ===");
        System.out.println("  Total livres : " + bookRepo.countAllBooks());

        categoryRepo.close();
        bookRepo.close();
        JpaUtil.close();
        System.out.println("\nApplication terminée.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers : éviter les doublons à chaque exécution
    // ─────────────────────────────────────────────────────────────────────────

    private static Category getOrCreateCategory(String name) {
        Category existing = categoryRepo.findByName(name);
        if (existing != null) {
            System.out.println("  [existant] Catégorie : " + name);
            return existing;
        }
        Category created = categoryRepo.create(Category.builder().name(name).build());
        System.out.println("  [créé]     Catégorie : " + name);
        return created;
    }

    private static Book getOrCreateBook(String title, String author,
                                         int year, int pages, Category category) {
        List<Book> existing = bookRepo.searchBooksByTitle(title);
        if (!existing.isEmpty()) {
            System.out.println("  [existant] Livre : " + title);
            return existing.get(0);
        }
        Book created = bookRepo.createBook(Book.builder()
                .title(title).author(author)
                .publicationYear(year).countPages(pages)
                .category(category).build());
        System.out.println("  [créé]     Livre : " + title + " | ISBN généré : " + created.getIsbn());
        return created;
    }
}
