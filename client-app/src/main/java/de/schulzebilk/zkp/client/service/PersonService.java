package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.client.rest.PepEntityClient;
import de.schulzebilk.zkp.core.model.Person;
import de.schulzebilk.zkp.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private final static Logger LOG = LoggerFactory.getLogger(PersonService.class);
    private final PepEntityClient<Person> pepPersonClient;
    private final String PERSON_URI = "/api/resource/person";

    @Autowired
    public PersonService(PepEntityClient<Person> pepPersonClient) {
        this.pepPersonClient = pepPersonClient;
    }

    public Person getPersonById(Long id, User user) {
        var uri = PERSON_URI + "/" + id;
        var person = pepPersonClient.getSingleEntityByUri(uri, user, Person.class);
        LOG.info("Person retrieved: {}", person);
        return person;
    }

    public Person createPerson(Person person, User user) {
        var createdPerson = pepPersonClient.createEntity(PERSON_URI, person, user, Person.class);
        LOG.info("Person created: {}", createdPerson);
        return createdPerson;
    }
}
