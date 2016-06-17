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
package com.eclecticlogic.pedal.provider.hibernate;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Adapter from https://forum.hibernate.org/viewtopic.php?t=946973 and
 * http://blog.xebia.com/2009/11/09/understanding-and-writing-hibernate-user-types/
 * 
 * @author kabram.
 *
 */
public class SetType extends ArrayType {

    public SetType() {
        super();
    }


    public SetType(String dialectPrimitiveName) {
        super(dialectPrimitiveName);
    }


    @Override
    public Class<?> returnedClass() {
        return Set.class;
    }


    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        Array sqlArray = rs.getArray(names[0]);

        if (rs.wasNull()) {
            return Collections.EMPTY_SET;
        } else {
            Set<Object> set = new HashSet<>();
            for (Object element : (Object[]) sqlArray.getArray()) {
                set.add((Object) element);
            }
            return set;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(final PreparedStatement statement, final Object object, final int i,
            SharedSessionContractImplementor session) throws HibernateException, SQLException {
        Connection connection = session.connection();
        Set<Object> set = (Set<Object>) object;
        Object[] array = set == null ? new Object[] {} : set.toArray();
        setArrayValue(statement, i, connection, array);
    }


    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return new HashSet<>();
        } else {
            return new HashSet<>((Collection<?>) value);
        }
    }

}
