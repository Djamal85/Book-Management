package sn.iage.isi.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import sn.iage.isi.entities.Book;

import java.util.List;
import java.util.Random;

public class BookRepository {

    EntityManager em = JpaUtil.getEntityManager();

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    public Book createBook(Book book) {
        EntityTransaction tx = em.getTransaction();
        Book b = Book.builder()
                .isbn(generateIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publicationYear(book.getPublicationYear())
                .countPages(book.getCountPages())
                .category(book.getCategory())
                .build();
        b.setUserCreated("admin");
        b.setUserUpdated("admin");
        try {
            tx.begin();
            em.persist(b);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        return b;
    }

    public List<Book> ListAllBooks() {
        return em
                .createQuery("SELECT b FROM Book b ORDER BY b.title ASC", Book.class)
                .getResultList();
    }

    public Book findBookById(int id) {
        Book book = em.find(Book.class, id);
        if (book == null)
            throw new EntityNotFoundException("Livre introuvable avec l'id : " + id);
        return book;
    }

    public Book findBookByIsbn(String isbn) {
        return em
                .createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
                .setParameter("isbn", isbn)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Book updateBook(int id, Book newBook) {
        EntityTransaction tx = em.getTransaction();
        Book book = findBookById(id);
        book.setTitle(newBook.getTitle());
        book.setAuthor(newBook.getAuthor());
        book.setPublicationYear(newBook.getPublicationYear());
        book.setCountPages(newBook.getCountPages());
        book.setCategory(newBook.getCategory());
        book.setUserUpdated("admin");
        try {
            tx.begin();
            em.merge(book);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
        return book;
    }

    public void deleteBook(int id) {
        EntityTransaction tx = em.getTransaction();
        Book book = findBookById(id);
        try {
            tx.begin();
            em.remove(book);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RECHERCHES
    // ─────────────────────────────────────────────────────────────────────────

    public List<Book> ListeBooksByCategory(String categoryName) {
        return em
                .createQuery("SELECT b FROM Book b WHERE LOWER(b.category.name) = LOWER(:name) ORDER BY b.title", Book.class)
                .setParameter("name", categoryName)
                .getResultList();
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return em
                .createQuery("SELECT b FROM Book b WHERE LOWER(b.title) LIKE :kw ORDER BY b.title", Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        return em
                .createQuery("SELECT b FROM Book b WHERE LOWER(b.author) LIKE :kw ORDER BY b.author", Book.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public List<Book> searchBooksAfterYear(int year) {
        return em
                .createQuery("SELECT b FROM Book b WHERE b.publicationYear > :year ORDER BY b.publicationYear ASC", Book.class)
                .setParameter("year", year)
                .getResultList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATISTIQUES
    // ─────────────────────────────────────────────────────────────────────────

    public List<Object[]> countBooksByCategory() {
        return em
                .createQuery("SELECT b.category.name, COUNT(b) FROM Book b GROUP BY b.category.name ORDER BY b.category.name", Object[].class)
                .getResultList();
    }

    public long countAllBooks() {
        return em
                .createQuery("SELECT COUNT(b) FROM Book b", Long.class)
                .getSingleResult();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GÉNÉRATION ISBN
    // ─────────────────────────────────────────────────────────────────────────

    private String generateIsbn() {
        String[] prefixes = {"978", "979"};
        Random random = new Random();
        String isbn;
        do {
            String prefix    = prefixes[random.nextInt(2)];
            String group     = String.valueOf(random.nextInt(2));
            String publisher = String.format("%04d", random.nextInt(10000));
            String title     = String.format("%04d", random.nextInt(10000));
            String base      = prefix + group + publisher + title;
            int checkDigit   = computeIsbn13CheckDigit(base);
            isbn = String.format("%s-%s-%s-%s-%d", prefix, group, publisher, title, checkDigit);
        } while (findBookByIsbn(isbn) != null);
        return isbn;
    }

    private int computeIsbn13CheckDigit(String base12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(base12.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int remainder = sum % 10;
        return remainder == 0 ? 0 : 10 - remainder;
    }

    public void close() {
        if (em != null && em.isOpen()) em.close();
    }
}
