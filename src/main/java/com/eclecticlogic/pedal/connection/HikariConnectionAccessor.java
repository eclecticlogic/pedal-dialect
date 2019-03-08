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
package com.eclecticlogic.pedal.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * For use with HikariCP
 * @author kabram.
 *
 */
public class HikariConnectionAccessor implements ConnectionAccessor {

    /**
     * @see com.eclecticlogic.pedal.connection.ConnectionAccessor#getRawConnection(java.sql.Connection)
     */
    @Override
    public Connection getRawConnection(Connection providerConnection) {
        try {
            return providerConnection.unwrap(Connection.class);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
