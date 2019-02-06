package pl.com.testme.testme.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;

@Controller
public class ManualController {

    @RequestMapping("/manual")
    public String manual(Model model, Principal principal){
        model.addAttribute("adminId", principal.getName());
        return "manual";
    }
}