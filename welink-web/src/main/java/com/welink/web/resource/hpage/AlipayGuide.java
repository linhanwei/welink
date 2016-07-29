package com.welink.web.resource.hpage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 15-4-6.
 */
@Controller
public class AlipayGuide {

    @RequestMapping(value = {"/api/h/1.0/alipayGuide.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        return "alipayGuide";
    }
}
