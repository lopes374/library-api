package com.sepol.libraryapi.model;

import com.sepol.libraryapi.model.repository.BookRepository;
import com.sepol.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base de dados com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenário
        String isbn = "123";
        Book book = Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
        entityManager.persist(book);

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        Assertions.assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base de dados com o isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){
        //cenário
        String isbn = "123";

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        Assertions.assertThat(exists).isFalse();

    }

}
