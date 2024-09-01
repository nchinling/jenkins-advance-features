package sg.edu.nus.iss.lovecalculator.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sg.edu.nus.iss.lovecalculator.model.Calculator;
import sg.edu.nus.iss.lovecalculator.service.CalculatorService;


@Controller
@RequestMapping(path="/calculate")
public class AppController {
    @Autowired 
    private CalculatorService cSvc;
    
    @GetMapping
    public String getScore(@RequestParam(required=true) String fname,
    @RequestParam(required=true) String sname, Model model) 
    throws IOException{
    Optional<Calculator> c = cSvc.getResult(fname, sname);
   
    model.addAttribute("calculatorresults", c.get());
    return "calculator";
    }

 
}

