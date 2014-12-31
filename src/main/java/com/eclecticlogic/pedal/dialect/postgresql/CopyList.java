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
package com.eclecticlogic.pedal.dialect.postgresql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * List of entities to be inserted via the CopyCommand
 * 
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
public class CopyList<E extends Serializable> extends ArrayList<E> {

    private String alternateTableName;


    public CopyList() {
        super();
    }


    public CopyList(Collection<? extends E> c) {
        super(c);
    }


    public CopyList(int initialCapacity) {
        super(initialCapacity);
    }


    /**
     * @return Alternate name of table to use.
     */
    public String getAlternateTableName() {
        return alternateTableName;
    }


    /**
     * @param name Alternate name to use for the table (useful for directly inserting into partition child 
     * tables.
     */
    public void setAlternateTableName(String name) {
        this.alternateTableName = name;
    }

}
