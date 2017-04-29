package com.robocubs4205.example.model;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
@Discriminator
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class B extends A{
    String bCol = "*";
}
