/**
 * Copyright (c) 2014 Eclectic Logic LLC
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
package com.eclecticlogic.pedal.provider.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import com.eclecticlogic.pedal.provider.Consumer;
import com.eclecticlogic.pedal.provider.Function;
import com.eclecticlogic.pedal.provider.ProviderAccessSpi;

/**
 * Get provider specific information.
 * @author kabram.
 *
 */
public class HibernateProviderAccessSpiImpl implements ProviderAccessSpi {

    private EntityManagerFactory emf;


    public void setEntityManagerFactory(EntityManagerFactory emf) {
        this.emf = emf;
    }


    @Override
    public String getSchemaName() {
        SessionFactory sf = emf.unwrap(HibernateEntityManagerFactory.class).getSessionFactory();
        SessionFactoryImplementor sfi = (SessionFactoryImplementor) sf;
        String schema = sfi.getSettings().getDefaultSchemaName();
        return schema == null ? "" : schema;
    }


    /**
     * @param entityClass Entity class for which the table name is required.
     * @return Table name if the entity class is a single table.
     */
    @Override
    public String getTableName(Class<? extends Serializable> entityClass) {
        SessionFactory sf = emf.unwrap(HibernateEntityManagerFactory.class).getSessionFactory();
        ClassMetadata metadata = sf.getClassMetadata(entityClass);
        if (metadata instanceof SingleTableEntityPersister) {
            SingleTableEntityPersister step = (SingleTableEntityPersister) metadata;
            return step.getTableName();
        } else {
            return null;
        }
    }


    @Override
    public void run(EntityManager entityManager, final Consumer<Connection> work) {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(new Work() {

            @Override
            public void execute(Connection connection) throws SQLException {
                work.accept(connection);
            }
        });
    }


    @Override
    public <R> R exec(EntityManager entityManager, final Function<Connection, R> work) {
        Session session = entityManager.unwrap(Session.class);
        return session.doReturningWork(new ReturningWork<R>() {

            @Override
            public R execute(Connection connection) throws SQLException {
                return work.apply(connection);
            }
        });
    }
}
