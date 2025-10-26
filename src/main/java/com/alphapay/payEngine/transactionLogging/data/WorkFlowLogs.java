package com.alphapay.payEngine.transactionLogging.data;

import com.alphapay.payEngine.common.bean.CommonBean;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name="workflow_logs")
public class WorkFlowLogs extends CommonBean {
    String requestId;
    String workflow;
    String request ;
    String rawResponse;
    String mappedResponse;

}
