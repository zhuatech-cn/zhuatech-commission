/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.controller;

import cn.zhuatech.commission.common.ApiResponse;
import cn.zhuatech.commission.model.CommissionRun;
import cn.zhuatech.commission.service.CommissionCalculationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/commission/runs")
@Validated
public class CommissionRunAdminController {
    private final CommissionCalculationService service;
    public CommissionRunAdminController(CommissionCalculationService service){this.service=service;}
    @PostMapping("/{id}/approve") ApiResponse<CommissionRun> approve(@PathVariable Long id,
        @RequestParam @NotBlank String remark){return ApiResponse.ok(service.approve(id,remark));}
}
