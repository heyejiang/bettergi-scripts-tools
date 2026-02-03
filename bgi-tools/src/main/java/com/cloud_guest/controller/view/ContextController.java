package com.cloud_guest.controller.view;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.aop.log.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @Author yan
 * @Date 2026/2/2 14:25:15
 * @Description
 */
@Tag(name="上下文相关")
@RestController
@RequestMapping("/context/")
public class ContextController {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @SysLog
    @Operation(summary = "获取CONTEXT_PATH")
    @GetMapping(value = "context.js", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getContextJs() {
        // 确保以 / 结尾
        String normalized = contextPath.endsWith("/") ? contextPath : contextPath + "/";
        return "window.CONTEXT_PATH = '" + normalized + "';";
    }
    @SysLog
    @Operation(summary = "获取bgi-tools版本")
    @GetMapping(value = "bgi-tools/version", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getBgiToolsVersion() {
        Environment env = SpringUtil.getBean(Environment.class);
        return env.getProperty("bgi-tools.version");
    }
}
