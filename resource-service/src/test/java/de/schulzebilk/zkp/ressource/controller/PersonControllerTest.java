package de.schulzebilk.zkp.ressource.controller;

import de.schulzebilk.zkp.ressource.model.Person;
import de.schulzebilk.zkp.ressource.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

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
        Person person = new Person("Alice", "Anderson");
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

}