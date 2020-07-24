package com.sepol.libraryapi.model;

import com.sepol.libraryapi.model.entity.Book;
import com.sepol.libraryapi.model.entity.Loan;
import com.sepol.libraryapi.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static com.sepol.libraryapi.model.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturned() {
        //cenário
        Loan loan = createAndPersistLoan(LocalDate.now());
        Book book = loan.getBook();

        //execução
        boolean exists = repository.existsByBookAndNotReturned(book);

        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest(){
        createAndPersistLoan(LocalDate.now());

        Page<Loan> result = repository.findBookIsbnOrCustomer(
                "123", "Fulano", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Deve obter empréstimo cuja data empréstimo for menor ou igual a três dias átras e não retornados")
    public void findByLoanDateLessThanNotReturned(){
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        List<Loan> result = repository.findByLoanDateLessThanNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);

    }

    @Test
    @DisplayName("Deve retornar vazio quando não houver emprestimos atrasados")
    public void notFindByLoanDateLessThanNotReturned(){
        Loan loan = createAndPersistLoan(LocalDate.now());

        List<Loan> result = repository.findByLoanDateLessThanNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();

    }

    public Loan createAndPersistLoan(LocalDate localDate) {
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(localDate).build();
        entityManager.persist(loan);

        return loan;
    }



}
