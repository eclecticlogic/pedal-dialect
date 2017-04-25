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
package com.eclecticlogic.pedal.dm;

import java.util.HashMap;
import java.util.Map;


/**
 * @author kabram.
 *
 */
public enum Color {

    RED("red"), //
    GREEN("green"), //
    BLUE("blue"), //
    BLACK("black"), // Black color will be converted to null value.
    ;
    
    private String code;


    private Color(String code) {
        this.code = code;
    }

    private static Map<String, Color> enumByCode = new HashMap<>();

    static {
        for (Color e : values()) {
            if (enumByCode.get(e.getCode()) != null) {
                throw new RuntimeException("Duplicate code found: " + e);
            }
            enumByCode.put(e.getCode(), e);
        }
    }


    public static Color forCode(String code) {
        Color value = enumByCode.get(code);
        if (value == null) {
            throw new RuntimeException("Null value sent.");
        }
        return value;
    }


    public String getCode() {
        return code;
    }
}
