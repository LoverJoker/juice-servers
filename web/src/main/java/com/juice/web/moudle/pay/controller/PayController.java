package com.juice.web.moudle.pay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求贤若饥 虚心若愚
 *
 * @author jokerliang
 * @date 2020-07-06 15:47
 */
@RestController
public class PayController {

    @GetMapping("/test")
    private String test() {

        return "success";
    }
}
