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
package com.eclecticlogic.pedal.dm;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author kabram.
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "planet")
public class Planet implements Serializable {

    private PlanetId id;
    private int distance;


    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "name", nullable = false)),
            @AttributeOverride(name = "position", column = @Column(name = "position", nullable = false)) })
    public PlanetId getId() {
        return id;
    }


    public void setId(PlanetId id) {
        this.id = id;
    }


    @Column(name = "distance")
    public int getDistance() {
        return distance;
    }


    public void setDistance(int distance) {
        this.distance = distance;
    }

}
