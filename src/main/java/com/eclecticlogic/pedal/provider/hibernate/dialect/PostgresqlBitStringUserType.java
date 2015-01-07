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
package com.eclecticlogic.pedal.provider.hibernate.dialect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.BitSet;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import com.eclecticlogic.pedal.provider.hibernate.AbstractMutableUserType;

/**
 * Maps a Postgresql bit-string to a java BitSet.
 * @author kabram.
 *
 */
public class PostgresqlBitStringUserType extends AbstractMutableUserType {

    public static final String BIT_LENGTH = "bitLength";
    
    
    public int getNumberOfBits() {
        try {
            return Integer.parseInt(getParameters().getProperty(BIT_LENGTH));   
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("You must provide a valid number for the number of bits "
                    + "using @Parameter");
        }
    }
    
    
    @Override
    public int[] sqlTypes() {
        return new int[] { Types.BIT };
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Class returnedClass() {
        return BitSet.class;
    }


    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        byte[] bytes = rs.getBytes(names[0]);

        if (rs.wasNull()) {
            return null;
        } else {
            BitSet bits = new BitSet(bytes.length);
            int index = 0;
            for (byte b : bytes) {
                // 1 and 0 are encoded as ascii values of '1' and '0'
                bits.set(index++, b - '0' != 0);
            }
            return bits;
        }
    }


    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        BitSet bits = (BitSet)value;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getNumberOfBits(); i++) {
            builder.append(bits.get(i) ? '1' : '0');
        }
        statement.setObject(index, builder.toString(), Types.OTHER);
    }


    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        } else {
            return ((BitSet)value).clone();
        }
    }

}
