package com.atm.controller;

import com.atm.model.User;
import com.atm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BackOfficeController {

    @Autowired
    UserService userService;

    @GetMapping("/backoffice")
    public String viewAdminPage(Model model) {
        return "index";
    }

    @GetMapping("/backoffice/login")
    public String viewLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/backoffice/customers")
    public String viewCustomerPage(Model model) {
        List<User> userList = userService.getAllCustomers();
        model.addAttribute("customers", userList);
        model.addAttribute("content", "fragments/customer");
        return "index";
    }
}
