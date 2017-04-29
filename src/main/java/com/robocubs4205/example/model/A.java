package com.robocubs4205.example.model;

import javax.jdo.annotations.*;

@PersistenceCapable
@Discriminator
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class A {
    @PrimaryKey()
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    long id;

    String aCol = "*";

}
