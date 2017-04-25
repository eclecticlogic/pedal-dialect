/*
 * Copyright (c) 2017 Eclectic Logic LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eclecticlogic.pedal.dialect.postgresql;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Allows collection of JPA entities to be written to the database using the Postgresql COPY command.
 *
 * Created by kabram
 */
public interface CopyCommand {

    /**
     * @param entityManager Entity manager reference.
     * @param entityList List of entities.
     * @param <E> JPA managed entity type.
     */
    <E extends Serializable> void insert(EntityManager entityManager, CopyList<E> entityList);

}
