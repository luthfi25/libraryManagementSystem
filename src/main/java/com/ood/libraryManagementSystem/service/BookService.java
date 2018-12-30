package com.ood.libraryManagementSystem.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ood.libraryManagementSystem.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("bookService")
public class BookService {

    private ObjectMapper mapper;
    private TypeReference<List<Book>> typeReference;
    private InputStream inputStream;
    private ClassLoader classLoader;

    @Autowired
    public BookService() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        typeReference = new TypeReference<List<Book>>(){};
        classLoader = getClass().getClassLoader();
    }

    public void writeToDatabase(List<Book> books){
        File databaseFile = new File(classLoader.getResource("json/books.json").getFile());

        try {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(databaseFile, JsonEncoding.UTF8);
            mapper.writeValue(generator, books);
            generator.flush();
            generator.close();
        } catch (IOException e) {
            System.out.println("Failed writing books " + e.toString());
        }
    }

    public List<Book> allBook() {
        inputStream = TypeReference.class.getResourceAsStream("/json/books.json");
        List<Book> books = new ArrayList<>();

        try {
            books = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            System.out.println("Failed reading books " + e.toString());
            return null;
        }

        return books;
    }

    public Book findBookByName(String name) {
        List<Book> books = allBook();

        if (books != null) {
            List<Book> desiredBook = books.stream().filter(b -> b.getName() != null && b.getName().equals(name)).collect(Collectors.toList());

            if (desiredBook.size() > 0) {
                return desiredBook.get(0);
            }
        }

        return null;
    }

    public Book findBookByISBN(String isbn) {
        List<Book> books = allBook();

        if (books != null) {
            List<Book> desiredBook = books.stream().filter(b -> b.getIsbn() != null && b.getIsbn().equals(isbn)).collect(Collectors.toList());

            if (desiredBook.size() > 0) {
                return desiredBook.get(0);
            }
        }

        return null;
    }

    public void saveBook(Book book) {
        List<Book> books = allBook();

        if (book == null) {
            return;
        }

        book.setId(books.size());
        books.add(book);
        writeToDatabase(books);
    }

    public boolean updateBook(Book book) {
        List<Book> books = allBook();

        if (books == null) {
            return false;
        }

        books.set(book.getId(), book);

        //CHECK DUPLICATE
        Set<String> ISBNs = new HashSet<>();
        for(Book b : books) {
            String is = b.getIsbn();
            if (!ISBNs.contains(is)) {
                ISBNs.add(is);
            } else {
                //DUPLICATE
                return false;
            }
        }

        writeToDatabase(books);
        return true;
    }

    public void deleteBook(String isbn) {
        List<Book> books = allBook();

        if (books == null) {
            return;
        }

        books.removeIf(b -> b.getIsbn().equals(isbn));

        //RECOMPILE ID
        int i = 0;
        for (Book b : books) {
            b.setId(i);
            i++;
        }

        writeToDatabase(books);
    }


}
