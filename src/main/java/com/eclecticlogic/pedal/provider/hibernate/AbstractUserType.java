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

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * Adapted from https://forum.hibernate.org/viewtopic.php?t=946973 and 
 * http://blog.xebia.com/2009/11/09/understanding-and-writing-hibernate-user-types/
 * 
 * Includes support for parameterized types.
 * 
 * @author kabram.
 *
 */
public abstract class AbstractUserType implements UserType, ParameterizedType {

    private Properties parameters = new Properties();


    public Properties getParameters() {
        return parameters;
    }


    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters != null) {
            for (Entry<Object, Object> entry : parameters.entrySet()) {
                this.parameters.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }


    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }


    @Override
    public int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x);
    }
}
