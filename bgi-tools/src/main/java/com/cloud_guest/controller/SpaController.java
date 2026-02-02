package com.cloud_guest.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author yan
 * @Date 2026/2/2 11:28:41
 * @Description
 */
@Controller
@RequestMapping("${server.servlet.context-path:/}")
public class SpaController {

    @GetMapping(value = "/**", produces = MediaType.TEXT_HTML_VALUE)
    public String forward() {
        return "forward:/index.html";
    }

    // 可选：根路径直接重定向到 /index.html
    @GetMapping(value = {"", "/"})
    public String index() {
        return "forward:/index.html";
    }
}