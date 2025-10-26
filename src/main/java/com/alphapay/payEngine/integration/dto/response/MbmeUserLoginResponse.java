package com.alphapay.payEngine.integration.dto.response;

import com.alphapay.payEngine.model.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
public class MbmeUserLoginResponse extends BaseResponse {
    private Long merchantId;
    private String userName;
    private String password = "";
    private Integer hid;
    private Map<String,String> responseData;
    private List<ResultItem> keys;


    public static class ResultItem {
        private String algorithm;
        @JsonProperty("isActive")
        private boolean active;
        @JsonProperty("isPrimary")
        private boolean primary;
        private String userRemarks;
        private String userApiKeyId;
        private String version;
        private String key;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public String getUserRemarks() {
            return userRemarks;
        }

        public void setUserRemarks(String userRemarks) {
            this.userRemarks = userRemarks;
        }

        public String getUserApiKeyId() {
            return userApiKeyId;
        }

        public void setUserApiKeyId(String userApiKeyId) {
            this.userApiKeyId = userApiKeyId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
