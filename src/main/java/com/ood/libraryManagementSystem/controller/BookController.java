package com.ood.libraryManagementSystem.controller;

import com.ood.libraryManagementSystem.model.Book;
import com.ood.libraryManagementSystem.model.User;
import com.ood.libraryManagementSystem.service.BookService;
import com.ood.libraryManagementSystem.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/books", method = RequestMethod.GET)
    public ModelAndView book() {
        ModelAndView modelAndView = new ModelAndView();
        List<Book> books = bookService.allBook();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        modelAndView.addObject("role", user.getRole());

        modelAndView.addObject("books", books);
        modelAndView.setViewName("books/show");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/books/create", method = RequestMethod.GET)
    public ModelAndView registerBook() {
        ModelAndView modelAndView = new ModelAndView();
        Book book = new Book();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("book", book);
        modelAndView.addObject("role", user.getRole());

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        modelAndView.setViewName("admin/books/create");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/books/create", method = RequestMethod.POST)
    public ModelAndView createNewBook(@Valid Book book, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        Book bookExists = bookService.findBookByISBN(book.getIsbn());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", user.getRole());

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        if (bookExists != null) {
            bindingResult.rejectValue("isbn", "error.book", "There is already a book registered witht the isbn provided");
        }

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("admin/books/create");
        } else {
            bookService.saveBook(book);
            modelAndView.addObject("successMessage", "Book has been registered successfully");
            modelAndView.addObject("book", new Book());
            modelAndView.setViewName("admin/books/create");
        }

        return modelAndView;
    }

    @RequestMapping(value = "/admin/books/update", method = RequestMethod.GET)
    public ModelAndView update(@RequestParam("isbn") String isbn) {
        ModelAndView modelAndView = new ModelAndView();
        Book book = bookService.findBookByISBN(isbn);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", user.getRole());

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        modelAndView.addObject("candidate", book);
        modelAndView.setViewName("home");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/books/update", method = RequestMethod.POST)
    public ModelAndView updateBook(@Valid Book book, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        boolean updateResult = bookService.updateBook(book);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", user.getRole());

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        if (!updateResult) {
            List<Book> books = bookService.allBook();
            List<Book> originalBooks = bookService.allBook();

            for (Book b:books) {
                b.setAuthor(StringUtils.abbreviate(b.getAuthor(),20));
                b.setName(StringUtils.abbreviate(b.getName(),20));
                b.setPublisher(StringUtils.abbreviate(b.getPublisher(),20));
                b.setIsbn(StringUtils.abbreviate(b.getIsbn(),20));
                b.setExplanation(StringUtils.abbreviate(b.getExplanation(),40));
            }

            modelAndView.addObject("role", user.getRole());
            modelAndView.addObject("books", books);
            modelAndView.addObject("originalBooks", originalBooks);

            modelAndView.addObject("failedMessage", "There is already a book registered with the isbn provided.");
            modelAndView.setViewName("home");
        } else {
            return new ModelAndView("redirect:/");
        }

        return modelAndView;
    }

    @RequestMapping(value = "/admin/books/delete", method = RequestMethod.POST)
    public ModelAndView deleteBook(@RequestBody String isbn) {
        isbn = isbn.substring(5);
        bookService.deleteBook(isbn);
        return new ModelAndView("redirect:/");
    }
}
