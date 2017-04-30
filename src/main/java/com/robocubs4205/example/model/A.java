package com.robocubs4205.example.model;

import javax.jdo.annotations.*;

@PersistenceCapable(detachable = "true")
@Discriminator
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class A {
    @PrimaryKey()
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    public long id;

    String aCol = "*";
    @Override
    public String toString(){
        return this.getClass().getSimpleName()+": "+String.valueOf(id);
    }
}
