package com.robocubs4205.example;

import com.robocubs4205.example.model.*;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.enhancer.EnhancerTask;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.datanucleus.store.StoreManager;
import org.h2.server.web.WebServlet;
import org.jhades.JHades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.util.Properties;

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
        private final PersistenceManagerFactory pmf;

        @Autowired
        InitBean(PersistenceManagerFactory pmf) {
            this.pmf = pmf;
            PersistenceManager pm = pmf.getPersistenceManager();
            A a = new A();
            pm.makePersistent(a);
            pm.makePersistent(new B());
            pm.makePersistent(new C());
            pm.makePersistent(new D());
            pm.makePersistent(new E());
        }
    }
}
