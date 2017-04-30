package com.robocubs4205.example;

import com.robocubs4205.example.model.*;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.h2.server.web.WebServlet;
import org.jhades.JHades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    PersistenceManagerFactory pmf() {

        new JHades().findClassByName("org.datanucleus.store.rdbms.RDBMSStoreManager");

        PersistenceUnitMetaData pumd = new PersistenceUnitMetaData("dynamic-unit",
                                                                   "RESOURCE_LOCAL",
                                                                   null);
        pumd.addClassName("com.robocubs4205.example.model.A");
        pumd.addClassName("com.robocubs4205.example.model.B");
        pumd.addClassName("com.robocubs4205.example.model.C");
        pumd.addClassName("com.robocubs4205.example.model.D");
        pumd.addClassName("com.robocubs4205.example.model.E");
        pumd.setExcludeUnlistedClasses();
        pumd.addProperty("javax.jdo.PersistenceManagerFactoryClass",
                         "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        pumd.addProperty("javax.jdo.option.ConnectionURL",
                         "jdbc:h2:mem:testdb");
        pumd.addProperty("javax.jdo.option.ConnectionDriverName", "org.h2.Driver");
        pumd.addProperty("javax.jdo.option.ConnectionUserName", "sa");
        pumd.addProperty("datanucleus.schema.autoCreateTables", "true");
        pumd.addProperty("datanucleus.schema.autoCreateColumns", "true");

        return new JDOPersistenceManagerFactory(pumd, null);
    }

    @Bean
    ServletRegistrationBean h2() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new WebServlet());
        bean.addUrlMappings("/console/*");
        return bean;
    }

    @Component
    class InitBean {
        private final ARepository aRepository;

        @Autowired
        InitBean(PersistenceManagerFactory pmf, ARepository aRepository) {
            this.aRepository = aRepository;
            A a = new A();
            aRepository.save(a);
            aRepository.save(new B());
            aRepository.save(Arrays.asList(new C(), new D(), new E()));

            System.out.println(aRepository.findAll());

            aRepository.delete(a);

            System.out.println(aRepository.findAll());

            A a2 = new B();
            a2.id = 2;

            aRepository.delete(a2);

            System.out.println(aRepository.findAll());

            A a3 = new B();
            a3.id = 27;

            aRepository.delete(a3);

            System.out.println(aRepository.findAll());

            System.out.println(aRepository.findOne(4L));
        }
    }
}
