package com.cloud_guest.controller.view;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author yan
 * @Date 2026/2/2 11:28:41
 * @Description
 */
@Controller
//@RequestMapping("/")
public class SpaController {

    // 根路径 / 或 /bgi 直接 forward
    @GetMapping(value = {"", "${server.servlet.context-path:/}"})
    public String root() {
        return "forward:/index.html";
    }

    // 所有非文件路径（不含 . 的路径）都 forward 到 index.html
    // 关键：[^\\.]* 排除 .js .css .html .png 等文件请求，让静态资源 handler 接管
    @GetMapping(value = "/{path:[^\\.]*}/**", produces = MediaType.TEXT_HTML_VALUE)
    public String forward(@PathVariable(required = false) String path) {
        return "forward:/index.html";
    }
}