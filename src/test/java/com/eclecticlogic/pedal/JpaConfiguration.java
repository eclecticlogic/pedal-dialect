/**
 * Copyright (c) 2014-2015 Eclectic Logic LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.eclecticlogic.pedal;

import com.eclecticlogic.pedal.connection.HikariConnectionAccessor;
import com.eclecticlogic.pedal.connection.TomcatJdbcConnectionAccessor;
import com.eclecticlogic.pedal.dialect.postgresql.CopyCommand;
import com.eclecticlogic.pedal.dialect.postgresql.CopyCommandImpl;
import com.eclecticlogic.pedal.provider.ProviderAccessSpi;
import com.eclecticlogic.pedal.provider.hibernate.HibernateProviderAccessSpiImpl;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * @author kabram.
 *
 */
@Configurable
@EnableAutoConfiguration
@ComponentScan
public class JpaConfiguration {

    /* 
     * Uncomment to test against HikariCP. Instantiation the hikari connection accessor in the copyCommand bean 
     * configuration below. 
     */
 
//    @Bean
//    DataSource hikari() {
//        HikariDataSource ds = new HikariDataSource();
//        ds.setJdbcUrl("jdbc:postgresql://localhost/pedal");
//        ds.setUsername("postgres");
//        ds.setPassword("postgres");
//        ds.setMaximumPoolSize(3);
//        return ds;
//    }

    /* 
     * Uncomment to test against Apache commons dbcp-2. Instantiation the hikari connection accessor in the copyCommand bean 
     * configuration below. 
     */
    
//    @Bean
//    DataSource dbcp() {
//        BasicDataSource bds = new BasicDataSource();
//        bds.setDriverClassName("org.postgresql.Driver");
//        bds.setUrl("jdbc:postgresql://localhost/pedal");
//        bds.setUsername("postgres");
//        bds.setPassword("postgres");
//        bds.setAccessToUnderlyingConnectionAllowed(true); // Very important!
//        return bds;
//    }
    

    @Bean
    HibernateProviderAccessSpiImpl hibernateProvider(EntityManagerFactory factory) {
        HibernateProviderAccessSpiImpl impl = new HibernateProviderAccessSpiImpl();
        impl.setEntityManagerFactory(factory);
        return impl;
    }


    /**
     * @return Introduce custom persistence unit manager so that we can use the orm.xml file to rename the table
     * mapping for Student.java and verify that we pick up the renamed mapping via ProviderAccess.
     */
    @Bean
    public PersistenceUnitManager persistenceUnitManager(DataSource dataSource) {
        DefaultPersistenceUnitManager manager = new DefaultPersistenceUnitManager();
        manager.setPersistenceXmlLocation("classpath:META-INF/pedal-test-persistence.xml");
        manager.setDefaultDataSource(dataSource);
        return manager;
    }


    @Bean
    public CopyCommand copyCommand(ProviderAccessSpi provider) {
        CopyCommandImpl command = new CopyCommandImpl();
        command.setProviderAccessSpi(provider);
        command.setConnectionAccessor(new TomcatJdbcConnectionAccessor());
        return command;
    }
}
