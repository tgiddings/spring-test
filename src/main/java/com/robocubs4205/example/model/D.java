package com.robocubs4205.example.model;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
//@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class D  extends B{
    String dCol = "*";
}
