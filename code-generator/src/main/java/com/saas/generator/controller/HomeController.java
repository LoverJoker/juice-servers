package com.saas.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * ${DESCRIPTION}
 *
 * @author adam
 * @date 2017-06-06 13:34
 */
@Controller
@RequestMapping("")
public class HomeController{

    @GetMapping(value = "index")
    public String index(Map<String,Object> map){
        return "index";
    }

    @GetMapping(value = "gener-vue")
    public String dist(){
        return "index1";
    }

    @GetMapping(value = "about")
    public String about(){
        return "about";
    }

    @GetMapping(value = "generator")
    public String user(){
        return "generator/list";
    }

}
