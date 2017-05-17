package com.robocubs4205.example;

import com.robocubs4205.example.model.A;
import com.robocubs4205.example.model.ARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AService {
    private final ARepository aRepository;

    @Autowired
    public AService(ARepository aRepository) {
        this.aRepository = aRepository;
    }

    @Transactional()
    public void foo() {
        A a = new A();
        aRepository.save(a);
        Iterable<A> as = aRepository.findAll();
        throw new RuntimeException();
    }
}
