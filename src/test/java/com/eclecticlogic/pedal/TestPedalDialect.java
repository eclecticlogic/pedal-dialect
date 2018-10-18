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

import com.eclecticlogic.pedal.dialect.postgresql.CopyCommand;
import com.eclecticlogic.pedal.dialect.postgresql.CopyList;
import com.eclecticlogic.pedal.dm.Color;
import com.eclecticlogic.pedal.dm.EmbedOverride;
import com.eclecticlogic.pedal.dm.EmbedSimple;
import com.eclecticlogic.pedal.dm.ExoticTypes;
import com.eclecticlogic.pedal.dm.Planet;
import com.eclecticlogic.pedal.dm.PlanetId;
import com.eclecticlogic.pedal.dm.Status;
import com.eclecticlogic.pedal.dm.Student;
import com.eclecticlogic.pedal.dm.VehicleIdentifier;
import com.eclecticlogic.pedal.provider.ProviderAccess;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kabram.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JpaConfiguration.class)
public class TestPedalDialect {

    private static final Logger LOG = LoggerFactory.getLogger(TestPedalDialect.class);

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

        Student student = new Student();
        student.setGpa(4.0f);
        student.setIdBase("custom_student");
        student.setMiddleName("joe");
        student.setName("schmoe");
        student.setZone("z'");
        student.setInsertedOn(new Date());
        entityManager.persist(student);

        ExoticTypes et = new ExoticTypes();
        et.setLogin("inserter");
        BitSet bs = new BitSet(7);
        bs.set(1);
        bs.set(3);
        bs.set(4);

        et.setCountries(bs);
        et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
        et.setScores(Lists.newArrayList(1L, 2L, 3L));
        et.setGpa(Lists.<Long> newArrayList());
        et.setStatus(Status.ACTIVE);
        et.setCustom("abc");
        et.setStudent(student);

        entityManager.persist(et);
        entityManager.flush();

        ExoticTypes loaded = entityManager.find(ExoticTypes.class, "inserter");
        Assert.assertNotNull(loaded);
        Assert.assertEquals(loaded.getLogin(), "inserter");
        Assert.assertEquals(loaded.getAuthorizations(), Sets.newHashSet("b", "a", "c"));
        Assert.assertEquals(loaded.getCountries().toString(), "{1, 3, 4}");
        Assert.assertEquals(loaded.getGpa(), Lists.newArrayList());
        Assert.assertEquals(loaded.getScores(), Lists.newArrayList(1L, 2L, 3L));
        Assert.assertEquals(loaded.getStatus(), Status.ACTIVE);
    }


    @Test
    @Transactional
    public void testInsertOfRenamedTable() {
        Student student = new Student();
        student.setIdBase("abc");
        student.setGpa(3.9f);
        student.setInsertedOn(new Date());
        student.setName("Joe Schmoe");
        student.setMiddleName("Que");
        student.setZone("d");

        entityManager.persist(student);

        Student s = entityManager.find(Student.class, "abc");
        Assert.assertEquals(s.getIdBase(), "abc");
        Assert.assertEquals(s.getGpa(), 3.9f, 0.001);
        Assert.assertEquals(s.getName(), "Joe Schmoe");
    }


    @Test
    @Transactional
    public void testCopyCommand() throws IOException {
        CopyList<ExoticTypes> list = new CopyList<>();

        Student student = new Student();
        student.setGpa(4.0f);
        student.setIdBase("exotic_student");
        student.setMiddleName("joe1");
        student.setName("schmoe1");
        student.setZone("z");
        student.setInsertedOn(new Date());
        entityManager.persist(student);

        // The copy-command can insert 100k of these per second.
        for (int i = 0; i < 10; i++) {
            ExoticTypes et = new ExoticTypes();
            et.setLogin("copyCommand" + i);
            BitSet bs = new BitSet(7);
            bs.set(1);
            bs.set(3);
            bs.set(4);
            et.setCountries(bs);
            et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
            if (i != 9) {
                et.setScores(Lists.newArrayList(1L, 2L, 3L));
            } else {
                et.setScores(Lists.<Long> newArrayList());
            }
            et.setStatus(Status.ACTIVE);
            et.setCustom("this will be made uppercase");
            et.setColor(Color.BLACK); // Black is converted to null. This is to test and ensure null value is
             // conversion is properly handled.
            et.setTotal(i * 10);
            et.setStudent(student);
            et.setImage(Files.readAllBytes(Paths.get(".", "src/test/resources/binary.data")));
            list.add(et);
        }

        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand0"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getCustom(),
                "THIS WILL BE MADE UPPERCASE");
        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand1"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getAuthorizations(),
                Sets.newHashSet("b", "c", "a"));

        // Nullable converted value should be written as null.
        Assert.assertNull(entityManager.find(ExoticTypes.class, "copyCommand0").getColor());
    }


    @Test
    @Transactional
    public void testBulkCopyCommand() {
        CopyList<ExoticTypes> list = new CopyList<>();

        Student student = new Student();
        student.setGpa(4.0f);
        student.setIdBase("exotic_student");
        student.setMiddleName("joe1");
        student.setName("schmoe1");
        student.setZone("z");
        student.setInsertedOn(new Date());
        entityManager.persist(student);

        int power = 0;
        double limit = Math.pow(2, 20);
        for (int i = 0; i <= limit; i++) {
            ExoticTypes et = new ExoticTypes();
            et.setLogin("copyCommand" + i);
            BitSet bs = new BitSet(7);
            bs.set(1);
            bs.set(3);
            bs.set(4);
            et.setCountries(bs);
            et.setAuthorizations(Sets.newHashSet("a", "b", "b", "c"));
            if (i != 9) {
                et.setScores(Lists.newArrayList(1L, 2L, 3L));
            } else {
                et.setScores(Lists.<Long> newArrayList());
            }
            et.setStatus(Status.ACTIVE);
            et.setCustom("this will be made uppercase");
            et.setColor(Color.BLACK); // Black is converted to null. This is to test and ensure null value is
            // conversion is properly handled.
            et.setTotal(i * 10);
            et.setStudent(student);
            // too much memory used.
            // et.setImage(Files.readAllBytes(Paths.get(".", "src/test/resources/binary.data")));
            list.add(et);

            if (list.size() % Math.pow(2, power) == 0) {
                copyCommand.insert(entityManager, list);
                list.clear();
                power++;
            }
        }

        //copyCommand.insert(entityManager, list);

        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand0"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getCustom(), "THIS WILL BE MADE UPPERCASE");
        Assert.assertNotNull(entityManager.find(ExoticTypes.class, "copyCommand1"));
        Assert.assertEquals(entityManager.find(ExoticTypes.class, "copyCommand0").getAuthorizations(), Sets.newHashSet("b", "c", "a"));

        // Nullable converted value should be written as null.
        Assert.assertNull(entityManager.find(ExoticTypes.class, "copyCommand0").getColor());
    }

    @Test
    @Transactional
    public void testAttributeOverrideWithCopyCommand() {
        CopyList<Student> list = new CopyList<>();
        {
            Student student = new Student();
            student.setGpa(4.0f);
            student.setIdBase("attrib");
            student.setMiddleName("joe1");
            student.setName("schmoe1");
            student.setZone("z");
            student.setInsertedOn(new Date());
            list.add(student);
        }
        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(Student.class, "attrib"));
    }


    @Test
    @Transactional
    public void testCopyCommandWithEmbeddedId() {
        CopyList<Planet> list = new CopyList<>();
        {
            Planet p = new Planet();
            PlanetId id = new PlanetId();
            id.setName("jupiter");
            id.setPosition(6);
            p.setId(id);
            p.setDistance(100);
            list.add(p);
        }
        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(Planet.class, new PlanetId("jupiter", 6)));
    }


    @Test
    @Transactional
    public void testCopyCommandEmbedSimple() {
        CopyList<EmbedSimple> list = new CopyList<>();
        EmbedSimple simple = new EmbedSimple();
        simple.setOwner("joe");
        VehicleIdentifier vi = new VehicleIdentifier();
        vi.setMake("Toyota");
        vi.setModel("corolla");
        vi.setYear(1990);
        simple.setIdentifier(vi);
        list.add(simple);
        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(EmbedSimple.class, "joe"));
    }


    @Test
    @Transactional
    public void testCopyCommandEmbedOverride() {
        CopyList<EmbedOverride> list = new CopyList<>();
        EmbedOverride embed = new EmbedOverride();
        embed.setOwner("joe");
        VehicleIdentifier vi = new VehicleIdentifier();
        vi.setMake("Toyota");
        vi.setModel("corolla");
        vi.setYear(1990);
        embed.setIdentifier(vi);
        list.add(embed);
        copyCommand.insert(entityManager, list);
        Assert.assertNotNull(entityManager.find(EmbedOverride.class, "joe"));
    }
}
