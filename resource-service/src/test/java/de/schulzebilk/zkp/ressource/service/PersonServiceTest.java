package de.schulzebilk.zkp.ressource.service;

import de.schulzebilk.zkp.ressource.model.Person;
import org.hibernate.annotations.processing.SQL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonServiceTest {

    @Autowired
    PersonService personService;

    @Test
    void findAll() {
        var persons = personService.findAll();
        assertNotNull(persons);
        assertTrue(persons.iterator().hasNext(), "Expected at least one person in the list");
    }

    @Test
    void findById() {
        var person = personService.findById(1L);
        assertNotNull(person, "Expected a person with ID 1 to be found");
        assertEquals(1L, person.getId(), "Expected the person ID to be 1");

        var nonExistentPerson = personService.findById(999L);
        assertNull(nonExistentPerson, "Expected no person to be found with ID 999");
    }

    @Test
    void save() {
        var person = new Person("Testy", "McTestface");
        var savedPerson = personService.save(person);
        assertNotNull(savedPerson, "Expected the saved person to not be null");
        assertEquals("Testy", savedPerson.getFirstname(), "Expected the first name to be 'Testy'");
        assertEquals("McTestface", savedPerson.getLastname(), "Expected the last name to be 'McTestface'");
    }

    @Test
    void deleteById() {
        var person = new Person("Delete", "Me");
        var savedPerson = personService.save(person);
        assertNotNull(savedPerson, "Expected the person to be saved successfully");

        personService.deleteById(savedPerson.getId());
        var deletedPerson = personService.findById(savedPerson.getId());
        assertNull(deletedPerson, "Expected the person to be deleted and not found");
    }
}