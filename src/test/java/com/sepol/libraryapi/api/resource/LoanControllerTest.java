package com.sepol.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sepol.libraryapi.api.dto.LoanDto;
import com.sepol.libraryapi.exception.BusinessException;
import com.sepol.libraryapi.model.entity.Book;
import com.sepol.libraryapi.model.entity.Loan;
import com.sepol.libraryapi.service.BookService;
import com.sepol.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo.")
    public void createLoanTest() throws  Exception {

        LoanDto dto = LoanDto.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given( bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1l).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given( loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(requestBuilder)
                .andExpect( status().isCreated())
                .andExpect( content().string("1"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
    public void invalidIsbnCreateLoanTes()  throws Exception{

        LoanDto dto = LoanDto.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given( bookService.getBookByIsbn("123"))
                .willReturn(Optional.empty());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(requestBuilder)
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( jsonPath( "errors[0]").value("Book not found for passed isbn"));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro emprestado")
    public void loanedBookErrorOnCreateLoanTest()  throws Exception{

        LoanDto dto = LoanDto.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("123").build();
        BDDMockito.given( bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book));

        BDDMockito.given( loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(requestBuilder)
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("errors", Matchers.hasSize(1)))
                .andExpect( jsonPath( "errors[0]").value("Book already loaned"));

    }
}
