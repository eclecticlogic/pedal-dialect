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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author kabram.
 *
 */
public abstract class ArrayType extends AbstractMutableUserType {

    public static final String DIALECT_PRIMITIVE_NAME = "arrayType";
    public static final String EMPTY_IS_NULL = "emptyIsNullHanding";


    public ArrayType() {
        super();
    }


    public ArrayType(String dialectPrimitiveName) {
        getParameters().setProperty(DIALECT_PRIMITIVE_NAME, dialectPrimitiveName);
    }


    protected String getDialectPrimitiveName() {
        return getParameters().getProperty(DIALECT_PRIMITIVE_NAME);
    }


    protected boolean isEmptyStoredAsNull() {
        return getParameters() == null || !"false".equalsIgnoreCase(getParameters().getProperty(EMPTY_IS_NULL));
    }


    @Override
    public int[] sqlTypes() {
        return new int[] { Types.ARRAY };
    }


    /**
     * Stores the array conforming to the EMPTY_IS_NULL directive.
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int, org.hibernate.engine.spi.SessionImplementor)
     */
    protected void setArrayValue(final PreparedStatement statement, final int i, Connection connection, Object[] array)
            throws SQLException {
        if (array == null || (isEmptyStoredAsNull() && array.length == 0)) {
            statement.setNull(i, Types.ARRAY);
        } else {
            statement.setArray(i, connection.createArrayOf(getDialectPrimitiveName(), array));
        }
    }
}
