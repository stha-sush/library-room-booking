package com.example.library.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addAuthFlags(Model model, Authentication authentication) {

        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        boolean isStudent = false;

        if (isLoggedIn) {
            for (GrantedAuthority a : authentication.getAuthorities()) {
                if ("ROLE_STUDENT".equals(a.getAuthority())) {
                    isStudent = true;
                    break;
                }
            }
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isStudent", isStudent);
    }
}