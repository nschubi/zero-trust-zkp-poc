package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.Person;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PersonServiceITest {

    @Autowired
    private PersonService personService;

    @Test
    void testGetPersonById() {
        String proverId = "alice";
        String proverKey = "password123";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

        Long personId = 1L;
        Person person = personService.getPersonById(personId, user);

        assertNotNull(person);
        assertEquals(personId, person.getId());
    }
}
