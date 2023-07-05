package com.sociallogintest.sociallogintest.controller;

import com.sociallogintest.sociallogintest.dto.UserDto;
import com.sociallogintest.sociallogintest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final Environment env;

    @Autowired
    public UserController(UserService userService, Environment env) {
        this.userService = userService;
        this.env = env;
    }

    // join
    @GetMapping("/users/new")
    public String createForm() {
        return "users/createUserForm";
    }

    @PostMapping("/users/new")
    public String createUser(UserDto userDto) {
        userService.createUser(userDto);
        return "redirect:/users";
    }

    // list
    @GetMapping("/users")
    public String findAll(Model model) {
        userService.findAllUser(model);
        return "users/userList";
    }

    // delete
    @GetMapping("/users/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    // google login link
    @GetMapping("/login/google")
    public String googleLoginLink() {
        return "redirect:"+env.getProperty("myprop.google_oauth_address");
    }

    // naver login link
    @GetMapping("/login/naver")
    public String naverLoginLink(){
        return "redirect:"+env.getProperty("myprop.naver_oauth_address");
    }

    // kakao login link
    @GetMapping("/login/kakao")
    public String kakaoLoginLink(){
        return "redirect:"+env.getProperty("myprop.kakao_oauth_address");
    }

    // login
    @GetMapping(value = "/login/oauth2/code/{registrationId}", produces = "application/json")
    public String socialLogin(@RequestParam String code, @PathVariable String registrationId) {
        userService.socialLogin(code, registrationId);
        return "redirect:/";
    }
}
