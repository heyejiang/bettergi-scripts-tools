package com.cloud_guest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @Author yan
 * @Date 2025/12/31 21:30:48
 * @Description
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ws代理类")
public class WsProxy {
    @NotBlank
    @Schema(description = "ws地址")
    private String url;
    @Schema(description = "token")
    private String token;
    @Schema(description = "消息体")
    @JsonProperty("bodyJson")
    private String bodyJson;
    @Schema(description = "消息体")
    @JsonProperty("body")
    private Map<String, Object> bodyMap;
}
