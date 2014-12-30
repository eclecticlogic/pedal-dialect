/** 
 *  Copyright (c) 2011-2014 Eclectic Logic LLC. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Eclectic Logic LLC ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Eclectic Logic LLC.
 *
 **/
package com.eclecticlogic.pedal;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;

import com.eclecticlogic.pedal.provider.hibernate.HibernateProviderAccessSpiImpl;

/**
 * @author kabram.
 *
 */
@Configurable
@EnableAutoConfiguration
@ComponentScan
public class TestJpaConfiguration {

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
    
    

}
