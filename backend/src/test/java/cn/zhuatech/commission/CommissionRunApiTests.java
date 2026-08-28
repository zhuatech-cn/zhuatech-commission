/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommissionRunApiTests {
    @Autowired MockMvc mvc;

    @Test
    void tieredCalculationApprovalPayoutAndClawbackFormClosedLoop() throws Exception {
        long id=create("COMM-RUN-001",800000,3,500000,5,30000,0,true,true,true);
        mvc.perform(post("/api/commission/runs/{id}/calculate",id).with(httpBasic("operator","operator123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.grossAmount").value(30000))
            .andExpect(jsonPath("$.data.payableAmount").value(30000))
            .andExpect(jsonPath("$.data.capped").value(false));
        mvc.perform(post("/api/commission/runs/{id}/submit",id).with(httpBasic("operator","operator123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.state").value("PENDING_REVIEW"));
        mvc.perform(post("/api/admin/commission/runs/{id}/approve",id).param("remark","越权")
                .with(httpBasic("operator","operator123"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/commission/runs/{id}/approve",id).param("remark","财务人力复核通过")
                .with(httpBasic("admin","admin123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.state").value("APPROVED"));
        mvc.perform(post("/api/commission/runs/{id}/payout",id).param("paymentReference","PAYROLL-001")
                .with(httpBasic("operator","operator123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.state").value("PAID"));
        mvc.perform(post("/api/commission/runs/{id}/clawback",id).with(httpBasic("operator","operator123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":5000,\"reason\":\"客户退款触发佣金追索\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.state").value("PARTIAL_CLAWBACK"))
            .andExpect(jsonPath("$.data.clawbackAmount").value(5000));
    }

    @Test
    void capAndGovernanceControlsAreEnforced() throws Exception {
        long id=create("COMM-RUN-CAP",2000000,5,100000,10,50000,2,true,true,true);
        mvc.perform(post("/api/commission/runs/{id}/calculate",id).with(httpBasic("operator","operator123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.payableAmount").value(50000))
            .andExpect(jsonPath("$.data.capped").value(true));
        mvc.perform(post("/api/commission/runs/{id}/submit",id).with(httpBasic("operator","operator123")))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/commission/runs/dashboard").with(httpBasic("operator","operator123")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.calculatedAmount").isNumber());
    }

    @Test
    void unapprovedPlanAndExcessiveClawbackAreRejected() throws Exception {
        long blocked=create("COMM-RUN-BLOCK",100000,3,100000,3,10000,0,false,true,true);
        mvc.perform(post("/api/commission/runs/{id}/calculate",blocked).with(httpBasic("operator","operator123")))
            .andExpect(status().isOk());
        mvc.perform(post("/api/commission/runs/{id}/submit",blocked).with(httpBasic("operator","operator123")))
            .andExpect(status().isConflict());

        long paid=create("COMM-RUN-PAID",100000,3,100000,3,10000,0,true,true,true);
        mvc.perform(post("/api/commission/runs/{id}/calculate",paid).with(httpBasic("operator","operator123"))).andExpect(status().isOk());
        mvc.perform(post("/api/commission/runs/{id}/submit",paid).with(httpBasic("operator","operator123"))).andExpect(status().isOk());
        mvc.perform(post("/api/admin/commission/runs/{id}/approve",paid).param("remark","批准")
            .with(httpBasic("admin","admin123"))).andExpect(status().isOk());
        mvc.perform(post("/api/commission/runs/{id}/payout",paid).param("paymentReference","PAY-002")
            .with(httpBasic("operator","operator123"))).andExpect(status().isOk());
        mvc.perform(post("/api/commission/runs/{id}/clawback",paid).with(httpBasic("operator","operator123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":999999,\"reason\":\"非法超额追索\"}"))
            .andExpect(status().isConflict());
    }

    private long create(String no,double revenue,double base,double threshold,double accelerator,double cap,
            int disputes,boolean approved,boolean locked,boolean compliance)throws Exception{
        var result=mvc.perform(post("/api/commission/runs").with(httpBasic("operator","operator123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"runNo\":\""+no+"\",\"planNo\":\"PLAN-2026-H2\","
                    +"\"beneficiary\":\"销售顾问A\",\"eligibleRevenue\":"+revenue+",\"baseRate\":"+base
                    +",\"acceleratorThreshold\":"+threshold+",\"acceleratorRate\":"+accelerator
                    +",\"capAmount\":"+cap+",\"disputedItems\":"+disputes+",\"planApproved\":"+approved
                    +",\"dataLocked\":"+locked+",\"complianceChecked\":"+compliance+"}"))
            .andExpect(status().isOk()).andReturn();
        var matcher=Pattern.compile("\\\"id\\\":(\\d+)").matcher(result.getResponse().getContentAsString());
        Assertions.assertTrue(matcher.find());return Long.parseLong(matcher.group(1));
    }
}
