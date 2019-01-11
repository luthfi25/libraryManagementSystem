package com.ood.libraryManagementSystem.controller;

import com.ood.libraryManagementSystem.model.Book;
import com.ood.libraryManagementSystem.model.User;
import com.ood.libraryManagementSystem.service.BookService;
import com.ood.libraryManagementSystem.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

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
            logger.info(user.getUsername() + " create a new book: " + book.toString());

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        logger.info(user.getUsername() + " update a book, result: " + book.toString());

        boolean updateResult = bookService.updateBook(book);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        logger.info(user.getUsername() + " want to delete a book with isbn: " + isbn);

        isbn = isbn.substring(5);
        bookService.deleteBook(isbn);

        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "/books/refresh/main", method = RequestMethod.GET)
    @ResponseBody
    public String refreshMain() {
        List<Book> books = bookService.allBook();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        String result = "";

        for (Book b:books) {
            b.setAuthor(StringUtils.abbreviate(b.getAuthor(),20));
            b.setName(StringUtils.abbreviate(b.getName(),20));
            b.setPublisher(StringUtils.abbreviate(b.getPublisher(),20));
            b.setIsbn(StringUtils.abbreviate(b.getIsbn(),20));
            b.setExplanation(StringUtils.abbreviate(b.getExplanation(),40));

            result += "<tr>\n";
            result += "<td>"+ b.getId() +"</td>\n";
            result += "<td>"+ b.getName() +"</td>\n";
            result += "<td>"+ b.getAuthor() +"</td>\n";
            result += "<td>"+ b.getPublisher() +"</td>\n";
            result += "<td>"+ b.getIsbn() +"</td>\n";
            result += "<td>"+ b.getExplanation() +"</td>\n";
            result += "<td><a type=\"button\" class=\"btn btn-primary btn-sm\" value=\"" + b.getIsbn() + "\" data-target=\"#bookDetailsModal\">Details</a>\n";

            if (user.getRole().equals("ADMIN")) {
                result +=  "<a type=\"button\" class=\"btn btn-success btn-sm\"  value=\"" + b.getIsbn() + "\" data-target=\"#bookUpdateModal\">Update</a>" +
                        "<form action=\"/admin/books/delete\" method=\"post\">\n" +
                        "<input type=\"hidden\" name=\"isbn\" value=\""+ b.getIsbn() + "\"/>\n" +
                        "<button type=\"submit\" class=\"btn btn-danger btn-sm\">Remove</button>\n" +
                        "</form></td>\n";
            }

            result += "</tr>";
        }

        return result;

    }

    @RequestMapping(value = "/admin/books/refresh/update", method = RequestMethod.GET)
    @ResponseBody
    public String refreshUpdate(@RequestParam("isbn") String isbn) {
        Book b = bookService.findBookByISBN(isbn);
        String result ="<div class=\"modal-dialog\" role=\"document\">\n" +
                "            <div class=\"modal-content\">\n" +
                "                <div class=\"modal-header\">\n" +
                "                    <h5 class=\"modal-title\" id=\"modal-update-name-" + b.getIsbn() + "\">Update Book Form</h5>\n" +
                "                    <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
                "                        <span aria-hidden=\"true\">&times;</span>\n" +
                "                    </button>\n" +
                "                </div>\n" +
                "                <div class=\"modal-body\">\n" +
                "                    <form autocomplete=\"off\" action=\"/admin/books/update\" method=\"post\" class=\"form-horizontal\" role=\"form\" id=\"bookUpdate-" + b.getIsbn() + "\">\n" +
                "                        <div class=\"form-group\">\n" +
                "                            <div class=\"col-sm-9\">\n" +
                "                                <input type=\"text\" value=\"" + b.getName() + "\" placeholder=\"Name\" id=\"name\" name=\"name\" class=\"form-control\"/>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "\n" +
                "                        <div class=\"form-group\">\n" +
                "                            <div class=\"col-sm-9\">\n" +
                "                                <input type=\"text\" value=\"" + b.getAuthor() + "\" placeholder=\"Author\" id=\"author\" name=\"author\" class=\"form-control\" />\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "\n" +
                "                        <div class=\"form-group\">\n" +
                "                            <div class=\"col-sm-9\">\n" +
                "                                <input type=\"text\" value=\"" + b.getPublisher() + "\" placeholder=\"Publisher\" id=\"publisher\" name=\"publisher\" class=\"form-control\"/>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "\n" +
                "                        <div class=\"form-group\">\n" +
                "                            <div class=\"col-sm-9\">\n" +
                "                                <input type=\"text\" value=\"" + b.getIsbn() + "\" placeholder=\"ISBN\" id=\"isbn\" name=\"isbn\" class=\"form-control\"/>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "\n" +
                "                        <div class=\"form-group\">\n" +
                "                            <div class=\"col-sm-9\">\n" +
                "                                <textarea form=\"bookUpdate-" + b.getIsbn() + "\" id=\"explanation\" name=\"explanation\">" + b.getExplanation() + "</textarea>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "\n" +
                "                        <input type=\"hidden\" value=\"" + b.getId() + "\" id=\"id\" name=\"id\">\n" +
                "\n" +
                "                    </form>\n" +
                "                </div>\n" +
                "                <div class=\"modal-footer\">\n" +
                "                    <div class=\"form-group\">\n" +
                "                        <div class=\"col-sm-12\">\n" +
                "                            <button type=\"submit\" class=\"btn btn-primary btn-block\" form=\"bookUpdate-" + b.getIsbn() + "\">Update Book</button>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        logger.info(user.getUsername() + " want to update a book with isbn: " + isbn);
        return result;
    }

    @RequestMapping(value = "/books/refresh/details", method = RequestMethod.GET)
    @ResponseBody
    public String refreshDetails(@RequestParam("isbn") String isbn) {
        Book b = bookService.findBookByISBN(isbn);
        String result = "    <div class=\"modal-dialog\" role=\"document\">\n" +
                "        <div class=\"modal-content\">\n" +
                "            <div class=\"modal-header\">\n" +
                "                <h5 class=\"modal-title\" id=\"modal-name-" + b.getIsbn() + "\" >" + b.getName() + "</h5>\n" +
                "                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
                "                    <span aria-hidden=\"true\">&times;</span>\n" +
                "                </button>\n" +
                "            </div>\n" +
                "            <div class=\"modal-body\">\n" +
                "                <div class=\"table-responsive\">\n" +
                "                    <table class=\"table table-striped table-sm\">\n" +
                "                        <tbody>\n" +
                "                        <tr>\n" +
                "                            <th>Author</th>\n" +
                "                            <td>" + b.getAuthor() + "</td>\n" +
                "                        </tr>\n" +
                "                        <tr>\n" +
                "                            <th>Publisher</th>\n" +
                "                            <td>" + b.getPublisher() + "</td>\n" +
                "                        </tr>\n" +
                "                        <tr>\n" +
                "                            <th>ISBN</th>\n" +
                "                            <td>" + b.getIsbn() + "</td>\n" +
                "                        </tr>\n" +
                "                        <tr>\n" +
                "                            <th>Explanation</th>\n" +
                "                            <td>" + b.getExplanation() + "</td>\n" +
                "                        </tr>\n" +
                "                        </tbody>\n" +
                "                    </table>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class=\"modal-footer\">\n";


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        if (user.getRole().equals("ADMIN")) {
            result += "                <a id=\"removeDetailsModal\" type=\"button\" class=\"btn btn-success btn-sm\"  value=\"" + b.getIsbn() + "\" data-target=\"#bookUpdateModal\">Update</a>\n" +
                    "                <form action=\"/admin/books/delete\" method=\"post\">\n" +
                    "                    <input type=\"hidden\" name=\"isbn\" value=\"" + b.getIsbn() + "\"/>\n" +
                    "                    <button type=\"submit\" class=\"btn btn-danger btn-sm\">Remove</button>\n" +
                    "                </form>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n";
        }

        return result;
    }
}
