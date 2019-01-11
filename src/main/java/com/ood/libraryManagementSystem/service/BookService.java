package com.ood.libraryManagementSystem.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ood.libraryManagementSystem.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
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
    private KeyService keyService;
    private boolean isFileLocked = false;

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    public BookService() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        typeReference = new TypeReference<List<Book>>(){};
        classLoader = getClass().getClassLoader();
        keyService = new KeyService();
    }

    public List<Book> encryptBooks(List<Book> books) {
        for (Book b: books) {
            b.setAuthor(keyService.encrypt(b.getAuthor()));
            b.setName(keyService.encrypt(b.getName()));
            b.setPublisher(keyService.encrypt(b.getPublisher()));
            b.setIsbn(keyService.encrypt(b.getIsbn()));
            b.setExplanation(keyService.encrypt(b.getExplanation()));
        }

        return books;
    }

    public List<Book> decryptBooks(List<Book> books) {
        for (Book b: books) {
            b.setAuthor(keyService.decrypt(b.getAuthor()));
            b.setName(keyService.decrypt(b.getName()));
            b.setPublisher(keyService.decrypt(b.getPublisher()));
            b.setIsbn(keyService.decrypt(b.getIsbn()));
            b.setExplanation(keyService.decrypt(b.getExplanation()));
        }

        return books;
    }

    public boolean writeToDatabase(List<Book> books){
        if (isFileLocked) {
            return false;
        }

        isFileLocked = true;
        books = encryptBooks(books);

        try {
            ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext();
            File databaseFile = appContext.getResource("classpath:json/books.json").getFile();

            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(databaseFile, JsonEncoding.UTF8);
            mapper.writeValue(generator, books);
            generator.flush();
            generator.close();
        } catch (IOException e) {
            logger.error("Failed writing books " + e.toString());
        } finally {
            isFileLocked = false;
        }

        return true;
    }

    public List<Book> allBook() {
        inputStream = TypeReference.class.getResourceAsStream("/json/books.json");
        List<Book> books = new ArrayList<>();

        try {
            books = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            logger.error("Failed reading books " + e.toString());
            return null;
        }

        books = decryptBooks(books);
        return books;
    }

//    @Async
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

//    @Async
    public void saveBook(Book book) {
        List<Book> books = allBook();

        if (book == null) {
            logger.error("failed creating book, book data is null");
            return;
        }

        book.setId(books.size());
        books.add(book);

        boolean succeedWrite = writeToDatabase(books);
        if (!succeedWrite) {
            logger.error("failed writing book, file is locked");
            return;
        }
    }

//    @Async
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
                logger.error("failed updating book, duplicate isbn. isbn: " + book.getIsbn());
                return false;
            }
        }

        boolean succeedWrite = writeToDatabase(books);
        if (!succeedWrite) {
            logger.error("failed writing book, file is locked");
            return false;
        }

        return true;
    }

//    @Async
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

        logger.info("sucessfully deleted book with isbn: " + isbn);

        boolean succeedWrite = writeToDatabase(books);
        if (!succeedWrite) {
            logger.error("failed writing book, file is locked");
            return;
        }
    }


}
