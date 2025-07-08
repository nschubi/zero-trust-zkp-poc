package de.schulzebilk.zkp.ressource.service;

import de.schulzebilk.zkp.core.model.resource.Person;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class PersonService {

    private HashMap<Long, Person> people = new HashMap<>();

    public PersonService() {
        people.put(1L, new Person(1L, "Alice", "Anderson"));
        people.put(2L, new Person(2L, "Bob", "Brown"));
        people.put(3L, new Person(3L, "Charlie", "Clark"));
    }

    public Person findById(Long id) {
        return people.get(id);
    }

    public Person save(Person person) {
        people.put(person.getId(), person);
        return person;
    }

    public void deleteById(Long id) {
        people.remove(id);
    }

    public List<Person> findAll() {
        return new ArrayList<>(people.values());
    }

}
