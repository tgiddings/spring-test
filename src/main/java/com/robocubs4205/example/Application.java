package com.robocubs4205.example;

import com.robocubs4205.example.model.*;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.h2.server.web.WebServlet;
import org.jhades.JHades;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jdo.JdoTransactionManager;
import org.springframework.orm.jdo.TransactionAwarePersistenceManagerFactoryProxy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.PersistenceManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableTransactionManagement
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    FactoryBean<PersistenceManagerFactory> pmf(@Qualifier("true") PersistenceManagerFactory truepmf) {

        TransactionAwarePersistenceManagerFactoryProxy pmf = new TransactionAwarePersistenceManagerFactoryProxy();
        pmf.setTargetPersistenceManagerFactory(truepmf);

        return pmf;
    }

    @Bean
    @Qualifier("true")
    PersistenceManagerFactory truepmf(){
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
        pumd.addProperty("datanucleus.generateSchema.database.mode","create");
        return new JDOPersistenceManagerFactory(pumd, null);
    }

    @Bean
    ServletRegistrationBean h2() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new WebServlet());
        bean.addUrlMappings("/console/*");
        return bean;
    }

    @Bean
    JdoTransactionManager tm(@Qualifier("true") PersistenceManagerFactory pmf){
        return new JdoTransactionManager(pmf);
    }

    @Component
    class InitBean {

        @Autowired
        InitBean(PersistenceManagerFactory pmf, ARepository aRepository, AService aService,JdoTransactionManager jtm) {
            try{
                aService.foo();
            }
            catch (Exception ignored){
                Iterable<A> as = aRepository.findAll();
                ignored.printStackTrace();
            }
        }
    }
}
