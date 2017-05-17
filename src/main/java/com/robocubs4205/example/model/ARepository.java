package com.robocubs4205.example.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;
import org.springframework.orm.jdo.TransactionAwarePersistenceManagerFactoryProxy;
import org.springframework.stereotype.Repository;
import org.springframework.util.ClassUtils;

import javax.jdo.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class ARepository implements CrudRepository<A, Long> {
    private final PersistenceManagerFactory pmf;

    @Autowired
    public ARepository(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    @Override
    public <S extends A> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("entity must not be null");
        try (PersistenceManager pm = pmf.getPersistenceManager()) {
            return pm.makePersistent(entity);
        }
    }

    @Override
    public <S extends A> Iterable<S> save(Iterable<S> entities) {
        if (entities == null)
            throw new IllegalArgumentException("entities iterable must not be null");
        try (PersistenceManager pm = pmf.getPersistenceManager()) {
            return pm.makePersistentAll(StreamSupport.stream(entities.spliterator(), false)
                                                     .collect(Collectors.toList()));
        }
    }

    @Override
    public A findOne(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        try (PersistenceManager pm = pmf.getPersistenceManager()) {
            return pm.getObjectById(A.class, id);
        }
    }

    @Override
    public boolean exists(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        try (PersistenceManager pm = pmf.getPersistenceManager()) {
            return (boolean) pm.newQuery(A.class)
                               .filter("this.id==:id")
                               .result("if(count(this))>0").execute(id);
        }
    }

    @Override
    public Iterable<A> findAll() {
        try (PersistenceManager pm = pmf.getPersistenceManager()) {
            boolean tx = pm.currentTransaction().isActive();
            return pm.newQuery(A.class).executeList();
        }
    }

    @Override
    public Iterable<A> findAll(Iterable<Long> ids) {
        if (ids == null) throw new IllegalArgumentException("ids iterable must not be null");
        try(PersistenceManager pm = pmf.getPersistenceManager()) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ids", ids);
            return pm.newQuery(A.class)
                     .filter(":ids.contains(id)")
                     .setNamedParameters(parameters)
                     .executeList();
        }
    }

    @Override
    public long count() {
        try(PersistenceManager pm = pmf.getPersistenceManager()){
            Query q = pm.newQuery(A.class).result("count(this)");
            return (long) q.execute();
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        try(PersistenceManager pm = pmf.getPersistenceManager()){
            Query<A> q = pm.newQuery(A.class).filter("this.id==:id");

            q.deletePersistentAll(id);
        }
    }

    @Override
    public void delete(A entity) {
        if (entity == null) throw new IllegalArgumentException("entity must not be null");
        delete(Collections.singleton(entity));
    }

    @Override
    public void delete(Iterable<? extends A> entities) {
        if (entities == null)
            throw new IllegalArgumentException("entities iterable must not be null");
        try(PersistenceManager pm = pmf.getPersistenceManager()){
            Collection<? extends A> transientAs =
            StreamSupport.stream(entities.spliterator(), false)
                         .filter(a -> JDOHelper
                                      .getObjectState(a) == ObjectState.TRANSIENT
                                      || JDOHelper
                                         .getObjectState(a) == ObjectState.TRANSIENT_CLEAN
                                      || JDOHelper
                                         .getObjectState(
                                         a) == ObjectState.TRANSIENT_DIRTY)
                         .collect(Collectors.toSet());

            Collection<? extends A> nontransientAs =
            StreamSupport.stream(entities.spliterator(), false)
                         .filter(a -> !(JDOHelper
                                        .getObjectState(a) == ObjectState.TRANSIENT
                                        || JDOHelper
                                           .getObjectState(
                                           a) == ObjectState.TRANSIENT_CLEAN
                                        || JDOHelper
                                           .getObjectState(
                                           a) == ObjectState.TRANSIENT_DIRTY)
                         )
                         .collect(Collectors.toSet());
            transientAs.forEach(a -> delete(a.id));
            pm.deletePersistentAll(nontransientAs);
        }
    }

    @Override
    public void deleteAll() {
        try(PersistenceManager pm = pmf.getPersistenceManager()){
            pm.newQuery(A.class).deletePersistentAll();
        }
    }
}
