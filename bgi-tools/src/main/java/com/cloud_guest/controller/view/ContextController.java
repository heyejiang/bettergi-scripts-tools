package com.cloud_guest.controller.view;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @Author yan
 * @Date 2026/2/2 14:25:15
 * @Description
 */
@RestController
@RequestMapping("/context/")
public class ContextController {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @GetMapping(value = "context.js", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getContextJs() {
        // 确保以 / 结尾
        String normalized = contextPath.endsWith("/") ? contextPath : contextPath + "/";
        return "window.CONTEXT_PATH = '" + normalized + "';";
    }
}
