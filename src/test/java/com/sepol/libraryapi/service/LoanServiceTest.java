package com.sepol.libraryapi.service;

import com.sepol.libraryapi.api.dto.LoanFilterDTO;
import com.sepol.libraryapi.exception.BusinessException;
import com.sepol.libraryapi.model.entity.Book;
import com.sepol.libraryapi.model.entity.Loan;
import com.sepol.libraryapi.model.repository.LoanRepository;
import com.sepol.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um emprétimo")
    public void saveLoanTest() {
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado.")
    public void loanBookSaveTest() {
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);

    }

    @Test
    @DisplayName("Deve obter as informação de um empréstimo pelo ID")
    public void getLoanDetaisTest() {
        //cenário
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        //execução
        Optional<Loan> result = service.getById(id);

        //verificações
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(loan.getId());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());

        verify(repository).findById(id);

    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {
        Loan loan = createLoan();
        loan.setId(1l);
        loan.setReturned(true);

        when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();
        verify(repository).save(loan);

    }

    @Test
    @DisplayName("Deve filtrar empréstimos pela propriedade.")
    public void findLoanTes(){
        //cenário
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();


        Loan loan = createLoan();
        loan.setId(1l);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        Mockito.when( repository.findBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class))).thenReturn(page);

        //execução
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //verificações
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    public static Loan createLoan() {
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }
}
