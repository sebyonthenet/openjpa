/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.enhance.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMappedById extends SingleEMFTestCase {
    public int numEmployees = 4;
    public int numDependentsPerEmployee = 2;
    public int numPersons = 4;

    public Map<Integer, Employee1> emps1 = new HashMap<Integer, Employee1>();
    public Map<String, Dependent1> deps1 = new HashMap<String, Dependent1>();
    public Map<Integer, Employee2> emps2 = new HashMap<Integer, Employee2>();
    public Map<String, Dependent2> deps2 = new HashMap<String, Dependent2>();
    public Map<String, Person1> persons1 = new HashMap<String, Person1>();
    public Map<String, MedicalHistory1> medicals1 = 
        new HashMap<String, MedicalHistory1>();
    public Map<String, Person2> persons2 = new HashMap<String, Person2>();
    public Map<String, MedicalHistory2> medicals2 = 
        new HashMap<String, MedicalHistory2>();

    public int eId1 = 1;
    public int dId1 = 1;
    public int eId2 = 1;
    public int dId2 = 1;
    public int pId1 = 1;
    public int mId1 = 1;
    public int pId2 = 1;
    public int mId2 = 1;

    public void setUp() throws Exception {
        super.setUp(DROP_TABLES, Dependent1.class, Employee1.class, 
            DependentId1.class, Dependent2.class, Employee2.class,
            DependentId2.class, EmployeeId2.class, MedicalHistory1.class,
            Person1.class, PersonId1.class, MedicalHistory2.class,
            Person2.class);
    }

    /**
     * This is spec 2.4.1.2 Example 1, case(b)
     */
    public void testMappedById1() {
        createObj1();
        findObj1();
        queryObj1();
    }

    /**
     * This is spec 2.4.1.2 Example 3, case(b)
     */
    public void testMappedById2() {
        createObj2();
        findObj2();
        queryObj2();
    }
    
    /**
     * This is spec 2.4.1.2 Example 5, case(b)
     */
    public void testMappedById3() {
        createObj3();
        findObj3();
        queryObj3();
    }

    /**
     * This is spec 2.4.1.2 Example 4, case(b) with generated key
     */
    public void testMappedById4() {
        createObj4();
        queryObj4();
    }

    public void createObj1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee1(em, eId1++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Employee1 createEmployee1(EntityManager em, int id) {
        Employee1 e = new Employee1();
        e.setEmpId(id);
        e.setName("emp_" + id);
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent1 d = createDependent1(em, dId1++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps1.put(id, e);
        return e;
    }

    public Dependent1 createDependent1(EntityManager em, int id, Employee1 e) {
        Dependent1 d = new Dependent1();
        DependentId1 did = new DependentId1();
        did.setName("dep_" + id);
        d.setId(did);
        d.setEmp(e);
        deps1.put(did.getName(), d);
        return d;
    }

    public void findObj1() {
        EntityManager em = emf.createEntityManager();
        Employee1 e = em.find(Employee1.class, 1);
        List<Dependent1> ds = e.getDependents();
        assertEquals(numDependentsPerEmployee, ds.size());
        Employee1 e0 = emps1.get(1);
        assertEquals(e0, e);
    }

    public void queryObj1() {
        queryDependent1();
    }

    public void queryDependent1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select d from Dependent1 d where d.id.name = 'dep_1' AND d.emp.name = 'emp_1'";
        Query q = em.createQuery(jpql);
        List<Dependent1> ds = q.getResultList();
        for (Dependent1 d : ds) {
            assertDependent1(d);
        }
        tran.commit();
        em.close();
    }

    public void assertDependent1(Dependent1 d) {
        DependentId1 id = d.getId();
        Dependent1 d0 = deps1.get(id.getName());
        if (d0.id.empPK == 0)
            d0.id.empPK = d0.emp.getEmpId();
        assertEquals(d0, d);
    }
    
    public void createObj2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numEmployees; i++)
            createEmployee2(em, eId2++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }
    
    public Employee2 createEmployee2(EntityManager em, int id) {
        Employee2 e = new Employee2();
        e.setEmpId(new EmployeeId2("f_" + id, "l_" + id));
        for (int i = 0; i < numDependentsPerEmployee; i++) {
            Dependent2 d = createDependent2(em, dId2++, e);
            e.addDependent(d);
            em.persist(d);
        }
        em.persist(e);
        emps2.put(id, e);
        return e;
    }

    public Dependent2 createDependent2(EntityManager em, int id, Employee2 e) {
        Dependent2 d = new Dependent2();
        DependentId2 did = new DependentId2();
        did.setName("dep_" + id);
        d.setEmp(e);
        d.setId(did);
        em.persist(d);
        deps2.put(did.getName(), d);
        return d;
    }

    public void findObj2() {
        EntityManager em = emf.createEntityManager();
        Employee2 e = em.find(Employee2.class, new EmployeeId2("f_1", "l_1"));
        List<Dependent2> ds = e.getDependents();
        assertEquals(numDependentsPerEmployee, ds.size());
        Employee2 e0 = emps2.get(1);
        assertEquals(e0, e);
    }

    public void queryObj2() {
        queryDependent2();
    }
    public void queryDependent2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        String jpql = "select d from Dependent2 d where d.id.name = 'dep_1' AND d.id.empPK.firstName = 'f_1'";
        Query q = em.createQuery(jpql);
        List<Dependent2> ds = q.getResultList();
        for (Dependent2 d : ds) {
            assertDependent2(d);
        }
        
        jpql = "select d from Dependent2 d where d.id.name = 'dep_1' AND d.emp.empId.firstName = 'f_1'";
        q = em.createQuery(jpql);
        ds = q.getResultList();
        for (Dependent2 d : ds) {
            assertDependent2(d);
        }        
        em.close();
    }

    public void assertDependent2(Dependent2 d) {
        DependentId2 did = d.getId();
        Dependent2 d0 = deps2.get(did.getName());
        DependentId2 did0 = d0.getId();
        did0.setEmpPK(d0.getEmp().getEmpId());
        assertEquals(d0, d);
    }
    
    public void createObj3() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson1(em, pId1++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person1 createPerson1(EntityManager em, int id) {
        Person1 p = new Person1();
        PersonId1 pid = new PersonId1();
        pid.setFirstName("f_" + id);
        pid.setLastName("l_" + id);
        p.setId(pid);
        MedicalHistory1 m = createMedicalHistory1(em, mId1++);
        m.setPatient(p);
        p.setMedical(m);
        em.persist(m);
        em.persist(p);
        persons1.put(pid.getFirstName(), p);
        medicals1.put(m.getPatient().getId().getFirstName(), m);
        return p;
    }

    public MedicalHistory1 createMedicalHistory1(EntityManager em, int id) {
        MedicalHistory1 m = new MedicalHistory1();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj3() {
        EntityManager em = emf.createEntityManager();
        PersonId1 pid = new PersonId1();
        pid.setFirstName("f_1");
        pid.setLastName("l_1");
        Person1 p = em.find(Person1.class, pid);
        Person1 p0 = persons1.get(pid.getFirstName());
        assertEquals(p0, p);
    }

    public void queryObj3() {
        queryMedicalHistory1();
    }

    public void queryMedicalHistory1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        String firstName = "f_1";
        tran.begin();
        String jpql = "select m from MedicalHistory1 m where m.patient.id.firstName = '" + firstName + "'";
        Query q = em.createQuery(jpql);
        List<MedicalHistory1> ms = q.getResultList();
        for (MedicalHistory1 m : ms) {
            assertMedicalHistory1(m, firstName);
        }
        
        jpql = "select m from MedicalHistory1 m where m.id.firstName = '" + firstName + "'";
        q = em.createQuery(jpql);
        ms = q.getResultList();
        for (MedicalHistory1 m : ms) {
            assertMedicalHistory1(m, firstName);
        }
        
        tran.commit();
        em.close();
    }

    public void assertMedicalHistory1(MedicalHistory1 m, String firstName) {
        MedicalHistory1 m0 = medicals1.get(firstName);
        assertEquals(m0, m);
    }
    
    public void createObj4() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson2(em, pId2++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person2 createPerson2(EntityManager em, int id) {
        Person2 p = new Person2();
        p.setName("p_" + id);
        
        MedicalHistory2 m = createMedicalHistory2(em, mId2++);
        m.setPatient(p);  // automatically set the id
        p.setMedical(m);
        em.persist(m);
        medicals2.put(m.getName(), m);

        em.persist(p);
        persons2.put(p.getName(), p);
        return p;
    }

    public MedicalHistory2 createMedicalHistory2(EntityManager em, int id) {
        MedicalHistory2 m = new MedicalHistory2();
        m.setName("medical_" + id);
        return m;
    }

    public void findObj4(long ssn) {
        EntityManager em = emf.createEntityManager();
        Person2 p = em.find(Person2.class, ssn);
        Person2 p1 = p.getMedical().getPatient();
        Assert.assertEquals(p1, p);
    }

    public void queryObj4() {
        queryMedicalHistory4();
    }

    public void queryMedicalHistory4() {
        EntityManager em = emf.createEntityManager();
        Map medicals = new HashMap();
        long ssn = 0;
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select m from MedicalHistory2 m";
        Query q = em.createQuery(jpql);
        List<MedicalHistory2> ms = q.getResultList();
        for (MedicalHistory2 m : ms) {
            ssn = m.getId();
        }
        tran.commit();
        em.close();
        
        em = emf.createEntityManager();
        tran = em.getTransaction();
        tran.begin();
        jpql = "select m from MedicalHistory2 m where m.patient.ssn = " + ssn;
        q = em.createQuery(jpql);
        ms = q.getResultList();
        for (MedicalHistory2 m : ms) {
            assertMedicalHistory2(m);
        }
        tran.commit();
        em.close();
        
        findObj4(ssn);
    }

    public void assertMedicalHistory2(MedicalHistory2 m) {
        String name = m.getName();
        MedicalHistory2 m0 = medicals2.get(name);
        MedicalHistory2 m1 = m.getPatient().getMedical();
        Assert.assertEquals(m1, m);
    }
    
}
