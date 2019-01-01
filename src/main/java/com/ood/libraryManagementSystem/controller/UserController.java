package com.ood.libraryManagementSystem.controller;

import com.ood.libraryManagementSystem.model.User;
import com.ood.libraryManagementSystem.service.UserService;
import org.apache.commons.lang.StringUtils;
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
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value="/admin/users", method = RequestMethod.GET)
    public ModelAndView user(){
        ModelAndView modelAndView = new ModelAndView();
        List<User> users = userService.allUser();

        for (User u:users) {
            u.setPassword(StringUtils.abbreviate(u.getPassword(),20));
            u.setName(StringUtils.abbreviate(u.getName(),20));
            u.setUsername(StringUtils.abbreviate(u.getUsername(),20));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("users", users);
        modelAndView.addObject("role", user.getRole());
        modelAndView.addObject("currentUser", user);

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + "<span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        modelAndView.setViewName("admin/users/show");
        return modelAndView;
    }

    @RequestMapping(value="/admin/users/create", method = RequestMethod.GET)
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", currentUser.getRole());

        if (currentUser.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + "<span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername());
        }

        modelAndView.addObject("user", user);
        modelAndView.setViewName("admin/users/create");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/create", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByUsername(user.getUsername());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", currentUser.getRole());

        if (currentUser.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername());
        }

        if (userExists != null) {
            bindingResult
                    .rejectValue("username", "error.user",
                            "There is already a user registered with the username provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("admin/users/create");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("admin/users/create");

        }
        return modelAndView;
    }

    @RequestMapping(value="/admin/users/update", method = RequestMethod.GET)
    public ModelAndView update(@RequestParam("username") String username){
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.findUserByUsername(username);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", currentUser.getRole());

        if (currentUser.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername());
        }

        modelAndView.addObject("candidate", user);
        modelAndView.setViewName("admin/users/update");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/update", method = RequestMethod.POST)
    public ModelAndView updateUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        boolean updateResult = userService.updateUser(user);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("role", currentUser.getRole());

        if (currentUser.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername());
        }

        if (!updateResult) {
            modelAndView.addObject("failedMessage", "There is already a user registered with the username provided.");
            modelAndView.addObject("candidate", user);
            modelAndView.setViewName("admin/users/update");
        } else {
            modelAndView.addObject("successMessage", "User has been updated successfully");
            modelAndView.addObject("candidate", user);
            modelAndView.setViewName("admin/users/update");

        }
        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/delete", method = RequestMethod.POST)
    public ModelAndView deleteUser(@RequestBody String username) {
        username = username.substring(9);
        userService.deleteUser(username);
        return new ModelAndView("redirect:/admin/users");
    }
}
