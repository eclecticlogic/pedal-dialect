/** 
 *  Copyright (c) 2011-2014 Eclectic Logic LLC. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Eclectic Logic LLC ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Eclectic Logic LLC.
 *
 **/
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
@SpringApplicationConfiguration(classes = TestJpaConfiguration.class)
public class TestHibernateProvider {

    @Autowired
    private ProviderAccess providerAccess;

    @PersistenceContext
    private EntityManager entityManager;

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
        et.setGpa(Lists.<Long>newArrayList());
        et.setStatus(Status.ACTIVE);
        
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
}
