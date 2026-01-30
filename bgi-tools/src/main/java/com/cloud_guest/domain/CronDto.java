package com.cloud_guest.domain;

import com.cloud_guest.aop.validator.ValidCron;
import com.cloud_guest.view.BasicJsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author yan
 * @Date 2026/1/12 17:40:15
 * @Description
 */
@Data @Schema(description = "cron解析类")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CronDto {
    @NotBlank(message ="key值不能为空" ,groups = {BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "key")
    private String key;
    @ValidCron(message = "cron表达式格式错误", groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "cron表达式")
    private String cronExpression;
    @NotNull(message = "开始时间戳不能为空",groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "开始时间戳")
    private Long startTimestamp;
    @NotNull(message = "结束时间戳不能为空", groups = {BasicJsonView.NextCronTimestampView.class,BasicJsonView.NextCronTimestampALLView.class})
    @Schema(description = "结束时间戳")
    private Long endTimestamp;
}
