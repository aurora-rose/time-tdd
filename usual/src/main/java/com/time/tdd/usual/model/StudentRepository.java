package com.time.tdd.usual.model;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

/**
 * repository
 *
 * @author XuJian
 * @date 2023-02-17 21:20
 **/
public class StudentRepository {

    private EntityManager manager;

    public StudentRepository(EntityManager manager) {
        this.manager = manager;
    }

    public Student save(Student student) {
        manager.persist(student);
        return student;
    }

    public Optional<Student> findById(long id) {
        return Optional.ofNullable(manager.find(Student.class, id));
    }

    public Optional<Student> findByEmail(String email) {
        TypedQuery<Student> query = manager.createQuery("SELECT s from Student s where s.email = :email", Student.class);
        return query.setParameter("email", email).getResultList().stream().findFirst();
    }

    public List<Student> all() {
        TypedQuery<Student> query = manager.createQuery("select s from Student", Student.class);
        return query.getResultList();
    }

}

