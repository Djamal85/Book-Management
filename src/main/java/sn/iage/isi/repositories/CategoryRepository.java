package sn.iage.isi.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import sn.iage.isi.entities.Category;

import java.util.List;

public class CategoryRepository {
    EntityManager em = JpaUtil.getEntityManager();

    public Category create(Category category) {
        EntityTransaction tx = em.getTransaction();
        Category c = Category.builder()
                .name(category.getName())
                .state(Boolean.TRUE)
                .build();
        c.setUserCreated("admin");
        c.setUserUpdated("admin");
        try {
            tx.begin();      //Debut de transaction
            em.persist(c);
            tx.commit();     //Validation de transaction
        }catch(Exception e) {
            tx.rollback();   //Annulation de la transaction
        }
        return c;
    }

    public List<Category> getAll() {
//        return em
//                .createQuery("SELECT c FROM Category c ORDER BY c.name ASC", Category.class)
//                .getResultList();
        return em.createNamedQuery("Category.findAll", Category.class).getResultList();
    }

    public Category getById(int id) {
        Category category = em.find(Category.class, id);
        if(category == null)
            throw new EntityNotFoundException("Category not found");
        return category;
    }

    public Category update(int id, Category newCategory) {
        EntityTransaction tx = em.getTransaction();
        Category cat = getById(id);
        if(cat != null){
            cat.setName(newCategory.getName());
            cat.setState(newCategory.isState());
            cat.setUserUpdated("user");
            try{
                tx.begin();
                em.merge(cat);
                tx.commit();
            }catch(Exception e) {
                tx.rollback();
            }
        }
        return cat;
    }

    public void delete(int id) {
        EntityTransaction tx = em.getTransaction();
        Category c = getById(id);
        try{
            tx.begin();
            em.remove(c);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }

    public List<Category> search(String keyword) {
        return em
                .createQuery("SELECT c FROM Category c WHERE LOWER(c.name) LIKE :kw ORDER BY c.name", Category.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    public long countCategories() {
        return em
                .createQuery("SELECT COUNT(c) FROM Category c", Long.class)
                .getSingleResult();
    }

    public List<Category> searchActiveCategories() {
        return em
                .createQuery("SELECT c FROM Category c WHERE c.state = true ORDER BY c.name", Category.class)
                .getResultList();
    }

    public Category findByName(String name) {
        return em
                .createQuery("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)", Category.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public void close() {
        if (em != null && em.isOpen()) em.close();
    }
}
