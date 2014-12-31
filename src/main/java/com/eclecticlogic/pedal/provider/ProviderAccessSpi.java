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
package com.eclecticlogic.pedal.provider;

import java.sql.Connection;

import javax.persistence.EntityManager;


/**
 * Service provider implementation specific methods.
 * 
 * @author kabram.
 *
 */
public interface ProviderAccessSpi extends ProviderAccess {

    /**
     * @param entityMangager JPA entity manager reference.
     * @param work Execute the work passing in the underlying JDBC connection object.
     */
    public void run(EntityManager entityManager, Consumer<Connection> work);


    /**
     * @param entityManager JPA entity manager reference.
     * @param work Work to execute passing in the underlying JDBC connection object.
     * @return the output of the work.
     */
    public <R> R exec(EntityManager entityManager, Function<Connection, R> work);
}
