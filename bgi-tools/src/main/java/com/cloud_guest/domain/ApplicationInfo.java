package com.cloud_guest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/3/2 22:42:44
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationInfo {
    public String applicationId;
    public Long datacenterId;
    //上报时间
    public Long timeStamp;

    public ApplicationInfo toReportedOnline() {
        return new ApplicationInfo(applicationId, datacenterId, System.currentTimeMillis());
    }
}
