package com.ood.libraryManagementSystem.controller;

import javax.validation.Valid;

import com.ood.libraryManagementSystem.model.Book;
import com.ood.libraryManagementSystem.model.User;
import com.ood.libraryManagementSystem.service.BookService;
import com.ood.libraryManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @RequestMapping(value={"/", "/login"}, method = RequestMethod.GET)
    public ModelAndView login(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof AnonymousAuthenticationToken)) {

            /* The user is logged in :) */
            return new ModelAndView("redirect:/home");
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping(value="/home", method = RequestMethod.GET)
    public ModelAndView home(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
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

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        modelAndView.setViewName("home");
        return modelAndView;
    }
}
