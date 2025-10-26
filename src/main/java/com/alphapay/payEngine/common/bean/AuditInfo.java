package com.alphapay.payEngine.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class AuditInfo {
    String ip;
    String wifiMAC;
    String bluetoothMAC;
    String appVersion;
    String deviceId;
    String deviceName;
    String sessionId;
    String acceptLanguage;
    String lat;
    String lng;
    Long userId;
}
