/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.controller;

import cn.zhuatech.commission.common.ApiResponse;
import cn.zhuatech.commission.model.CommissionRun;
import cn.zhuatech.commission.service.CommissionCalculationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commission/runs")
public class CommissionRunController {
    private final CommissionCalculationService service;
    public CommissionRunController(CommissionCalculationService service){this.service=service;}
    @GetMapping ApiResponse<List<CommissionRun>> list(){return ApiResponse.ok(service.list());}
    @PostMapping ApiResponse<CommissionRun> create(@Valid @RequestBody CommissionCalculationService.CreateRequest request){return ApiResponse.ok(service.create(request));}
    @PostMapping("/{id}/calculate") ApiResponse<CommissionCalculationService.CalculationResult> calculate(@PathVariable Long id){return ApiResponse.ok(service.calculate(id));}
    @PostMapping("/{id}/submit") ApiResponse<CommissionRun> submit(@PathVariable Long id){return ApiResponse.ok(service.submit(id));}
    @PostMapping("/{id}/payout") ApiResponse<CommissionRun> payout(@PathVariable Long id,@RequestParam String paymentReference){return ApiResponse.ok(service.payout(id,paymentReference));}
    @PostMapping("/{id}/clawback") ApiResponse<CommissionRun> clawback(@PathVariable Long id,@Valid @RequestBody CommissionCalculationService.ClawbackRequest request){return ApiResponse.ok(service.clawback(id,request));}
    @GetMapping("/dashboard") ApiResponse<CommissionCalculationService.Dashboard> dashboard(){return ApiResponse.ok(service.dashboard());}
}
