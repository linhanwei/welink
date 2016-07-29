package com.welink.web.resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 15-5-5.
 */
@Controller
public class Error {

    @RequestMapping(value = {"/h/error.htm"}, produces = "text/html;charset=UTF-8")
    public String userStaticLogin(HttpServletRequest request, HttpServletResponse response,
                                  ModelMap model) {
        model.addAttribute("name", "hello yonder");
        String session = request.getParameter("session");
        model.addAttribute("session", session);
        return "error";
    }
}
