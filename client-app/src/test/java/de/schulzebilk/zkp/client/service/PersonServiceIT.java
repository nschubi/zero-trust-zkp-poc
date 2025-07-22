package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.Person;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class PersonServiceIT {

    @Autowired
    private PersonService personService;

    @Test
    void testGetPersonById() {
        String proverId = "alice";
        String proverKey = "password123";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

        User user2 = new User("charlie", "secretpass333", AuthType.PASSWORD);

        Long personId = 1L;
        Person person = null;
        StopWatch stopFiatShamir = new StopWatch();
        stopFiatShamir.start("Fiat-Shamir Authentication");
        person = personService.getPersonById(personId, user);
        stopFiatShamir.stop();

        System.out.println("Fiat-Shamir Authentication took: " + stopFiatShamir.getTotalTimeMillis() + " ms");

        assertNotNull(person);
        assertEquals(personId, person.getId());

        StopWatch stopPassword = new StopWatch();
        stopPassword.start("Password Authentication");
        person = personService.getPersonById(personId, user2);
        stopPassword.stop();
        System.out.println("Password Authentication took: " + stopPassword.getTotalTimeMillis() + " ms");

        assertNotNull(person);
        assertEquals(personId, person.getId());
    }
}
