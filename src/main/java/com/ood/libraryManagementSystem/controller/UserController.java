package com.ood.libraryManagementSystem.controller;

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
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
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
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
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
            logger.info(currentUser.getUsername() + " create new user: " + user.toString());

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());
        logger.info(currentUser.getUsername() + " successfully updated an user, result: " + user.toString());

        boolean updateResult = userService.updateUser(user);
        modelAndView.addObject("role", currentUser.getRole());

        if (currentUser.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + currentUser.getUsername());
        }

        if (!updateResult) {
            List<User> users = userService.allUser();

            for (User u:users) {
                u.setPassword(StringUtils.abbreviate(u.getPassword(),20));
                u.setName(StringUtils.abbreviate(u.getName(),20));
                u.setUsername(StringUtils.abbreviate(u.getUsername(),20));
            }

            modelAndView.addObject("currentUser", currentUser);
            modelAndView.addObject("users", users);

            modelAndView.addObject("failedMessage", "There is already a user registered with the username provided.");
            modelAndView.setViewName("admin/users/show");
        } else {
            return new ModelAndView("redirect:/admin/users/");
        }

        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/delete", method = RequestMethod.POST)
    public ModelAndView deleteUser(@RequestBody String username) {
        username = username.substring(9);
        userService.deleteUser(username);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findUserByUsername(auth.getName());
        logger.info(currentUser.getUsername() + " about to delete user with username: " + username);

        return new ModelAndView("redirect:/admin/users/");
    }

    @RequestMapping(value = "/admin/users/search", method = RequestMethod.GET)
    public ModelAndView searchUser() {
        ModelAndView modelAndView = new ModelAndView();
        List<User> users = userService.allUser();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        modelAndView.addObject("users", users);
        modelAndView.addObject("role", user.getRole());
        modelAndView.addObject("currentUser", user);

        if (user.getRole().equals("ADMIN")) {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername() + " <span class=\"badge badge-success\">Admin</span>");
        } else {
            modelAndView.addObject("userName", "Welcome, " + user.getUsername());
        }

        modelAndView.setViewName("admin/users/search");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/users/refresh/main", method = RequestMethod.GET)
    @ResponseBody
    public String refreshMain() {
        List<User> users = userService.allUser();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        String result = "";

        for (User u:users) {
            u.setPassword(StringUtils.abbreviate(u.getPassword(),20));
            u.setName(StringUtils.abbreviate(u.getName(),20));
            u.setUsername(StringUtils.abbreviate(u.getUsername(),20));

            result += "<tr>\n" +
                    "                        <td>" + u.getId() + "</td>\n" +
                    "                        <td>" + u.getName() + "</td>\n" +
                    "                        <td>" + u.getUsername() + "</td>\n" +
                    "                        <td>" + u.getPassword() + "</td>\n" +
                    "                        <td>\n";

            if (u.getRole().equals("ADMIN")) {
                result += "                            <span class=\"badge badge-success\">Admin</span>\n";
            } else {
                result += "                            <span class=\"badge badge-secondary\">Regular</span>\n";
            }

            result += "                        </td>\n" +
                    "                        <td>\n";

            if (user.getRole().equals("ADMIN") && !user.getUsername().equals(u.getUsername())) {
                result +="                            <a value=\"" + u.getUsername() + "\" type=\"button\" class=\"btn btn-success btn-sm\" data-target=\"#userUpdateModal\">Update</a>\n" +
                        "                            <form action=\"/admin/users/delete\" method=\"post\">\n" +
                        "                                <input type=\"hidden\" name=\"username\" value=\"" + u.getUsername() + "\"/>\n" +
                        "                                <button type=\"submit\" class=\"btn btn-danger btn-sm\">Remove</button>\n" +
                        "                            </form>\n";
            }

            result += "                        </td>\n" +
                    "                    </tr>";
        }

        return result;
    }

    @RequestMapping(value = "/admin/users/refresh/update", method = RequestMethod.GET)
    @ResponseBody
    public String refreshUpdate(@RequestParam("username") String username) {
        User u = userService.findUserByUsername(username);
        String result = "<div class=\"modal-dialog\" role=\"document\">\n" +
                "        <div class=\"modal-content\">\n" +
                "            <div class=\"modal-header\">\n" +
                "                <h5 class=\"modal-title\" id=\"modal-update-name-" + u.getUsername() + "\">Update User Form</h5>\n" +
                "                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
                "                    <span aria-hidden=\"true\">&times;</span>\n" +
                "                </button>\n" +
                "            </div>\n" +
                "            <div class=\"modal-body\">\n" +
                "                <form autocomplete=\"off\" action=\"/admin/users/update\" method=\"post\" class=\"form-horizontal\" role=\"form\" id=\"userUpdate-" + u.getUsername() + "\">\n" +
                "                    <div class=\"form-group\">\n" +
                "                        <div class=\"col-sm-9\">\n" +
                "                            <input type=\"text\" value=\"" + u.getName() + "\" placeholder=\"Name\" name=\"name\" id=\"name\"\n" +
                "                                   class=\"form-control\" />\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"form-group\">\n" +
                "                        <div class=\"col-sm-9\">\n" +
                "                            <input type=\"text\" value=\"" + u.getUsername() + "\" placeholder=\"Username\" id=\"username\" name=\"username\"\n" +
                "                                   class=\"form-control\" />\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"form-group\">\n" +
                "                        <div class=\"col-sm-9\">\n" +
                "                            <input type=\"text\" value=\"" + u.getPassword() + "\"\n" +
                "                                   placeholder=\"Password\" id=\"password\" name=\"password\" class=\"form-control\" />\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"form-group\">\n" +
                "                        <div class=\"col-sm-9\">\n" +
                "                            <select class=\"form-control\" value=\"" + u.getRole() + "\" id=\"role\" name=\"role\">\n";

        if (u.getRole().equals("ADMIN")) {
            result += "                                <option selected=\"selected\">ADMIN</option>\n" +
                    "                                <option>REGULAR</option>\n";
        } else {
            result += "                                <option>ADMIN</option>\n" +
                    "                                <option selected=\"selected\">REGULAR</option>\n";
        }

        result += "                            </select>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <input type=\"hidden\" value=\"" + u.getId() + "\" id=\"id\" name=\"id\">\n" +
                "\n" +
                "                </form>\n" +
                "            </div>\n" +
                "            <div class=\"modal-footer\">\n" +
                "                <div class=\"form-group\">\n" +
                "                    <div class=\"col-sm-12\">\n" +
                "                        <button type=\"submit\" class=\"btn btn-primary btn-block\" form=\"userUpdate-" + u.getUsername() + "\">Update User</button>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());

        logger.info(user.getUsername() + " about to update user with username: " + u.getUsername());
        return result;
    }

    @RequestMapping(value = "/admin/users/refresh/details", method = RequestMethod.GET)
    @ResponseBody
    public String refreshDetails(@RequestParam("username") String username) {
        User u = userService.findUserByUsername(username);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(auth.getName());
        String result = "<div style=\"margin-top:50px;\" class=\"table-responsive\" id=\"table-" + u.getUsername() + "\">\n" +
                    "                    <table class=\"table table-striped table-sm\">\n" +
                    "                        <tbody>\n" +
                    "                        <tr>\n" +
                    "                            <th>ID</th>\n" +
                    "                            <td>" + u.getId() + "</td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <th>Full Name</th>\n" +
                    "                            <td>" + u.getName() + "</td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <th>Username</th>\n" +
                    "                            <td>" + u.getUsername() + "</td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <th>Password</th>\n" +
                    "                            <td>" + u.getPassword() + "</td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <th>Role</th>\n" +
                    "                            <td>\n";

            if (u.getRole().equals("ADMIN")) {
                result += "                                <span class=\"badge badge-success\">Admin</span>\n";
            } else {
                result += "                                <span class=\"badge badge-secondary\">Regular</span>\n";
            }

            result += "                            </td>\n" +
                    "                        </tr>\n";

            if (user.getRole().equals("ADMIN") && !user.getUsername().equals(u.getUsername())) {
                result += "                        <tr>\n" +
                        "                            <th>Action</th>\n" +
                        "                            <td>\n" +
                        "                                <a type=\"button\" class=\"btn btn-success btn-sm\" data-target=\"#userUpdateModal\" value=\"" + u.getUsername() + "\">Update</a>\n" +
                        "                                <form action=\"/admin/users/delete\" method=\"post\">\n" +
                        "                                    <input type=\"hidden\" name=\"username\" value=\"" + u.getUsername() + "\"/>\n" +
                        "                                    <button type=\"submit\" class=\"btn btn-danger btn-sm\">Remove</button>\n" +
                        "                                </form>\n" +
                        "                            </td>\n" +
                        "                        </tr>\n";
            }

            result += "                        </tbody>\n" +
                    "                    </table>\n" +
                    "                </div>";

        return result;
    }

    @RequestMapping(value = "/admin/users/all", method = RequestMethod.GET)
    @ResponseBody
    public List<User> fetchUser() {
        List<User> users = userService.allUser();
        return users;
    }
}
