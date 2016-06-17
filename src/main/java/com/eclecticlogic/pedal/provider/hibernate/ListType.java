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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Adapter from https://forum.hibernate.org/viewtopic.php?t=946973 and
 * http://blog.xebia.com/2009/11/09/understanding-and-writing-hibernate-user-types/
 * 
 * @author kabram.
 *
 */
public class ListType extends ArrayType {

    public ListType() {
        super();
    }


    public ListType(String dialectPrimitiveName) {
        super(dialectPrimitiveName);
    }


    @Override
    public Class<?> returnedClass() {
        return List.class;
    }


    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        Array sqlArray = rs.getArray(names[0]);

        if (rs.wasNull()) {
            return Collections.EMPTY_LIST;
        } else {
            List<Object> list = new ArrayList<>();
            for (Object element : (Object[]) sqlArray.getArray()) {
                list.add(element);
            }
            return list;
        }
    }


    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int, org.hibernate.engine.spi.SessionImplementor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(final PreparedStatement statement, final Object object, final int i,
            SharedSessionContractImplementor session) throws HibernateException, SQLException {
        Connection connection = session.connection();
        List<Object> list = (List<Object>) object;
        Object[] array = list == null ? null : list.toArray();
        setArrayValue(statement, i, connection, array);
    }


    /**
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>((Collection<?>) value);
        }
    }

}
