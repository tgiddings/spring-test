package com.robocubs4205.example.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.jdo.*;
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
        return doTransactional((pm, tx) -> {
            return pm.makePersistent(entity);
        });
    }

    @Override
    public <S extends A> Iterable<S> save(Iterable<S> entities) {
        if (entities == null) throw new IllegalArgumentException("entities iterable must not be null");
        return doTransactional((pm, tx) -> {
            List<S> as = new ArrayList<>();
            entities.forEach(entity -> {
                pm.makePersistent(entity);
                /*
                makePersistent may modify tables, which is not possible if uncommitted changes to the
                table's rows exist
                 */
                tx.commit();
                tx.begin();
                as.add(entity);
            });
            return as;
        });
    }

    @Override
    public A findOne(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        return doTransactional((pm, tx) -> {
            A a = (A) pm.newQuery(A.class)
                              .filter("this.id==:id")
                              .setParameters(id)
                              .executeUnique();
            System.out.println(a.getClass().getSimpleName());
            return (A) a;
        });
    }

    @Override
    public boolean exists(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        return doTransactional((pm, tx) -> {
            return (boolean) pm.newQuery(A.class)
                               .filter("this.id==:id")
                               .result("if(count(this))>0").execute(id);
        });
    }

    @Override
    public Iterable<A> findAll() {
        return doTransactional((pm, tx) -> {
            Query<A> q = pm.newQuery(A.class);
            return q.executeList();
        });

    }

    @Override
    public Iterable<A> findAll(Iterable<Long> ids) {
        if (ids == null) throw new IllegalArgumentException("ids iterable must not be null");
        return doTransactional((pm, tx) -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ids", ids);
            Query<A> q = pm.newQuery(A.class)
                           .filter(":ids.contains(id)")
                           .setNamedParameters(parameters);
            return q.executeList();
        });
    }

    @Override
    public long count() {
        return (long) doTransactional((pm, tx) -> {
            Query q = pm.newQuery(A.class).result("count(this)");

            return q.execute();
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        doTransactional((pm, tx) -> {
            Query<A> q = pm.newQuery(A.class).filter("this.id==:id");

            q.deletePersistentAll(id);
        });
    }

    @Override
    public void delete(A entity) {
        if (entity == null) throw new IllegalArgumentException("entity must not be null");
        delete(Collections.singleton(entity));
    }

    @Override
    public void delete(Iterable<? extends A> entities) {
        if (entities == null) throw new IllegalArgumentException("entities iterable must not be null");
        doTransactional((pm, tx) -> {
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
        });
    }

    @Override
    public void deleteAll() {
        doTransactional((pm, tx) -> {
            Query<A> q = pm.newQuery(A.class);

            q.deletePersistentAll();
        });
    }

    private <T> T doTransactional(PersistenceFunction<T> pf) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            T t = pf.call(pm, tx);
            tx.commit();
            return t;
        } finally {
            if (tx.isActive()) tx.rollback();
        }
    }

    private void doTransactional(ActionPersistenceFunction pf) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.setDetachAllOnCommit(true);
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pf.call(pm, tx);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
        }
    }

    @FunctionalInterface
    private interface PersistenceFunction<T> {
        T call(PersistenceManager pm, Transaction tx);
    }

    @FunctionalInterface
    private interface ActionPersistenceFunction {
        void call(PersistenceManager pm, Transaction tx);
    }
}
