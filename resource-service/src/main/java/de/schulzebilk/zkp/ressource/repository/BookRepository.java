package de.schulzebilk.zkp.ressource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import de.schulzebilk.zkp.ressource.model.Book;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
