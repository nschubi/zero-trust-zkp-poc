package de.schulzebilk.zkp.ressource.controller;

import de.schulzebilk.zkp.ressource.model.Person;
import de.schulzebilk.zkp.ressource.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonControllerTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonController personController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPerson_found() {
        Person person = new Person("Bob", "Andrews");
        when(personService.findById(1L)).thenReturn(person);

        ResponseEntity<Person> response = personController.getPerson(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(person, response.getBody());
    }

    @Test
    void getPerson_notFound() {
        when(personService.findById(2L)).thenReturn(null);

        ResponseEntity<Person> response = personController.getPerson(2L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void getAllPersons() {
        List<Person> persons = Arrays.asList(
            new Person("Bob", "Andrews"),
            new Person("Justus", "Jonas")
        );
        when(personService.findAll()).thenReturn(persons);

        ResponseEntity<Iterable<Person>> response = personController.getAllPersons();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(persons, response.getBody());
        verify(personService).findAll();
    }

    @Test
    void createPerson() {
        Person person = new Person("New", "Person");
        Person savedPerson = new Person("New", "Person");
        when(personService.save(person)).thenReturn(savedPerson);

        ResponseEntity<Person> response = personController.createPerson(person);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(savedPerson, response.getBody());
        verify(personService).save(person);
    }

    @Test
    void updatePerson_found() {
        Person existingPerson = new Person("Old", "Name");
        Person updatedPerson = new Person("New", "Name");
        when(personService.findById(1L)).thenReturn(existingPerson);
        when(personService.save(existingPerson)).thenReturn(existingPerson);

        ResponseEntity<Person> response = personController.updatePerson(1L, updatedPerson);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(existingPerson, response.getBody());
        assertEquals("New", existingPerson.getFirstname());
        assertEquals("Name", existingPerson.getLastname());
        verify(personService).findById(1L);
        verify(personService).save(existingPerson);
    }

    @Test
    void updatePerson_notFound() {
        Person updatedPerson = new Person("New", "Name");
        when(personService.findById(2L)).thenReturn(null);

        ResponseEntity<Person> response = personController.updatePerson(2L, updatedPerson);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(personService).findById(2L);
        verify(personService, never()).save(any());
    }

    @Test
    void deletePerson_found() {
        Person existingPerson = new Person("Delete", "Me");
        when(personService.findById(1L)).thenReturn(existingPerson);

        ResponseEntity<Void> response = personController.deletePerson(1L);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(personService).findById(1L);
        verify(personService).deleteById(1L);
    }

    @Test
    void deletePerson_notFound() {
        when(personService.findById(2L)).thenReturn(null);

        ResponseEntity<Void> response = personController.deletePerson(2L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(personService).findById(2L);
        verify(personService, never()).deleteById(any());
    }

}