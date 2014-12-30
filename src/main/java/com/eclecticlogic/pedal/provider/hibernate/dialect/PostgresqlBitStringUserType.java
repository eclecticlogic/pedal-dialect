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
package com.eclecticlogic.pedal.provider.hibernate.dialect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.eclecticlogic.pedal.provider.hibernate.AbstractMutableUserType;

/**
 * @author kabram.
 *
 */
public class PostgresqlBitStringUserType extends AbstractMutableUserType {

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.BIT };
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return List.class;
    }


    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        byte[] bytes = rs.getBytes(names[0]);

        if (rs.wasNull()) {
            return Collections.EMPTY_LIST;
        } else {
            List<Boolean> list = new ArrayList<>();
            for (byte b : bytes) {
                // 1 and 0 are encoded as ascii values of '1' and '0'
                list.add(b - '0' != 0);
            }
            return list;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        List<Boolean> list = (List<Boolean>) value;
        StringBuilder builder = new StringBuilder();
        for (Boolean b : list) {
            builder.append(b ? '1' : '0');
        }
        statement.setObject(index, builder.toString(), Types.OTHER);
    }


    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>((Collection<?>) value);
        }
    }

}
