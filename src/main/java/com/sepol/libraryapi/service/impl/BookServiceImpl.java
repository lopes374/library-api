package com.sepol.libraryapi.service.impl;

import com.sepol.libraryapi.model.entity.Book;
import com.sepol.libraryapi.exception.BussinessException;
import com.sepol.libraryapi.model.repository.BookRepository;
import com.sepol.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if(repository.existsByIsbn(book.getIsbn())) {
            throw new BussinessException("Isbn já cadastrado");
        }
        return repository.save(book);
    }
}
