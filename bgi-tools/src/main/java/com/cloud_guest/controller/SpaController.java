package com.cloud_guest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author yan
 * @Date 2026/2/2 11:28:41
 * @Description
 */
@Controller
public class SpaController {
    @SuppressWarnings("SpringPathVariableNotConsumed")
    @GetMapping("${server.servlet.context-path:/}/**/{path:[^.]*}")  // 推荐
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
