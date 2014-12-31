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
package com.eclecticlogic.pedal;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eclecticlogic.pedal.dialect.postgresql.CopyCommand;
import com.eclecticlogic.pedal.dialect.postgresql.CopyList;
import com.eclecticlogic.pedal.dm.ExoticTypes;
import com.eclecticlogic.pedal.dm.Status;
import com.eclecticlogic.pedal.dm.Student;
import com.eclecticlogic.pedal.provider.ProviderAccess;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author kabram.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JpaConfiguration.class)
public class TestPedalDialect {

    @Autowired
    private ProviderAccess providerAccess;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CopyCommand copyCommand;


    @Test
    public void testSchemaName() {
        Assert.assertEquals(providerAccess.getSchemaName(), "dialect");
        Assert.assertEquals(providerAccess.getTableName(ExoticTypes.class), "dialect.exotic_types");
        // The student class mapping has been modified via META-INF/pedal-test-orm.xml
        Assert.assertEquals(providerAccess.getTableName(Student.class), "dialect.graduate_student");
    }


    @Test
    @Transactional
    public void insertTestForCustomTypes() {
        ExoticTypes et = new ExoticTypes();
        et.setLogin("inserter");
        et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList(1L, 2L, 3L));
        et.setGpa(Lists.<Long> newArrayList());
        et.setStatus(Status.ACTIVE);
        et.setCustom("abc");

        entityManager.persist(et);

        ExoticTypes loaded = entityManager.find(ExoticTypes.class, "inserter");
        Assert.assertNotNull(loaded);
        Assert.assertEquals(loaded.getLogin(), "inserter");
        Assert.assertEquals(loaded.getAuthorizations(), Sets.newHashSet("b", "a", "c"));
        Assert.assertEquals(loaded.getCountries(), Lists.newArrayList(false, false, true, false, false, false, true));
        Assert.assertEquals(loaded.getGpa(), Lists.newArrayList());
        Assert.assertEquals(loaded.getScores(), Lists.newArrayList(1L, 2L, 3L));
        Assert.assertEquals(loaded.getStatus(), Status.ACTIVE);
    }


    @Test
    @Transactional
    public void testInsertOfRenamedTable() {
        Student student = new Student();
        student.setId("abc");
        student.setGpa(3.9f);
        student.setInsertedOn(new Date());
        student.setName("Joe Schmoe");
        student.setMiddleName("Que");
        student.setZone("d");

        entityManager.persist(student);

        Student s = entityManager.find(Student.class, "abc");
        Assert.assertEquals(s.getId(), "abc");
        Assert.assertEquals(s.getGpa(), 3.9f, 0.001);
        Assert.assertEquals(s.getName(), "Joe Schmoe");
    }


    @Test
    @Transactional
    public void testCopyCommand() {
        CopyList<ExoticTypes> list = new CopyList<>();

        // The copy-command can insert 100k of these per second.
        for (int i = 0; i < 10; i++) {
            ExoticTypes et = new ExoticTypes();
            et.setLogin("copyCommand" + i);
            et.setCountries(Lists.newArrayList(false, false, true, false, false, false, true));
            et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
            if (i != 9) {
                et.setScores(Lists.newArrayList(1L, 2L, 3L));
            } else {
                et.setScores(Lists.<Long> newArrayList());
            }
            et.setStatus(Status.ACTIVE);
            et.setCustom("this will be made uppercase");
            list.add(et);
        }

        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand0"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getCustom(),
                "THIS WILL BE MADE UPPERCASE");
        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand1"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getAuthorizations(),
                Sets.newHashSet("b", "c", "a"));
    }
}
