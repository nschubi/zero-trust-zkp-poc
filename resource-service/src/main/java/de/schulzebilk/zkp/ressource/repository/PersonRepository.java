package de.schulzebilk.zkp.ressource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import de.schulzebilk.zkp.ressource.model.Person;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
