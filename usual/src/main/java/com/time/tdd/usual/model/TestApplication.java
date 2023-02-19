package com.time.tdd.usual.model;

import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * test
 *
 * @author XuJian
 * @date 2023-02-17 21:33
 **/
public class TestApplication {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("student");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        StudentRepository studentRepository = new StudentRepository(entityManager);
        Student john = studentRepository.save(new Student(null, "john", "smith", "john.smith@email.com"));
        entityManager.getTransaction().commit();
        System.out.println(john.getId());

        Optional<Student> loaded = studentRepository.findById(john.getId());
        System.out.println(loaded);
    }
}

