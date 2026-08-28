/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.service;

import cn.zhuatech.commission.model.*;
import cn.zhuatech.commission.repository.*;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;

@Service
public class CommissionCalculationService {
    private final CommissionRunRepository runs;private final AuditLogRepository audits;
    public CommissionCalculationService(CommissionRunRepository runs,AuditLogRepository audits){
        this.runs=runs;this.audits=audits;
    }
    public List<CommissionRun> list(){return runs.findAllByOrderByUpdatedAtDesc();}

    @Transactional
    public CommissionRun create(CreateRequest r){
        if(runs.findByRunNo(r.runNo()).isPresent())throw conflict("佣金批次号已存在");
        if(r.acceleratorRate().compareTo(r.baseRate())<0)throw bad("加速费率不能低于基础费率");
        var item=runs.save(new CommissionRun(r.runNo(),r.planNo(),r.beneficiary(),r.eligibleRevenue(),
            r.baseRate(),r.acceleratorThreshold(),r.acceleratorRate(),r.capAmount(),r.disputedItems(),
            r.planApproved(),r.dataLocked(),r.complianceChecked()));
        audit("创建佣金计算批次",item,r.beneficiary());return item;
    }

    @Transactional
    public CalculationResult calculate(Long id){
        var item=get(id);require(item,"DRAFT","只有草稿批次允许计算");
        BigDecimal base=item.getEligibleRevenue().min(item.getAcceleratorThreshold())
            .multiply(item.getBaseRate()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
        BigDecimal accelerated=item.getEligibleRevenue().subtract(item.getAcceleratorThreshold())
            .max(BigDecimal.ZERO).multiply(item.getAcceleratorRate())
            .divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
        BigDecimal gross=base.add(accelerated);BigDecimal payable=gross.min(item.getCapAmount());
        item.calculate(payable);audit("执行佣金计算",item,"毛额="+gross+", 应付="+payable);
        return new CalculationResult(item,gross,payable,gross.compareTo(item.getCapAmount())>0);
    }

    @Transactional
    public CommissionRun submit(Long id){
        var item=get(id);require(item,"CALCULATED","仅已计算批次可以提交");
        if(!item.isPlanApproved())throw conflict("激励方案尚未事前审批");
        if(!item.isDataLocked())throw conflict("业绩数据尚未锁定");
        if(!item.isComplianceChecked())throw conflict("合规检查尚未通过");
        if(item.getDisputedItems()>0)throw conflict("仍有佣金归属争议未解决");
        item.submit();audit("提交佣金复核",item,"控制门禁全部通过");return item;
    }

    @Transactional
    public CommissionRun approve(Long id,String remark){
        var item=get(id);require(item,"PENDING_REVIEW","只有待复核批次可以批准");
        item.approve();audit("批准佣金结果",item,remark);return item;
    }

    @Transactional
    public CommissionRun payout(Long id,String paymentReference){
        var item=get(id);require(item,"APPROVED","仅已批准批次允许发放");
        item.pay();audit("完成佣金发放",item,paymentReference);return item;
    }

    @Transactional
    public CommissionRun clawback(Long id,ClawbackRequest r){
        var item=get(id);if(!Set.of("PAID","PARTIAL_CLAWBACK").contains(item.getState()))throw conflict("仅已发放佣金允许追索");
        BigDecimal remaining=item.getCalculatedAmount().subtract(item.getClawbackAmount());
        if(r.amount().compareTo(remaining)>0)throw conflict("追索金额不能超过剩余已发放金额");
        item.clawback(r.amount());audit("佣金追索",item,r.reason());return item;
    }

    public Dashboard dashboard(){
        BigDecimal payable=runs.findAll().stream().map(CommissionRun::getCalculatedAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal clawback=runs.findAll().stream().map(CommissionRun::getClawbackAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new Dashboard(runs.count(),runs.countByState("PENDING_REVIEW"),runs.countByState("PAID"),payable,clawback);
    }

    private CommissionRun get(Long id){return runs.findById(id).orElseThrow(()->
        new ResponseStatusException(HttpStatus.NOT_FOUND,"佣金批次不存在"));}
    private void require(CommissionRun item,String state,String message){if(!state.equals(item.getState()))throw conflict(message);}
    private ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
    private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
    private void audit(String action,CommissionRun item,String detail){
        var auth=SecurityContextHolder.getContext().getAuthentication();
        audits.save(new AuditLog("COMMISSION",action,item.getRunNo(),auth==null?"system":auth.getName(),detail));
    }

    public record CreateRequest(@NotBlank @Size(max=40) String runNo,@NotBlank @Size(max=40) String planNo,
        @NotBlank @Size(max=60) String beneficiary,@NotNull @PositiveOrZero BigDecimal eligibleRevenue,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal baseRate,
        @NotNull @PositiveOrZero BigDecimal acceleratorThreshold,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal acceleratorRate,
        @NotNull @Positive BigDecimal capAmount,@PositiveOrZero int disputedItems,
        boolean planApproved,boolean dataLocked,boolean complianceChecked){}
    public record ClawbackRequest(@NotNull @Positive BigDecimal amount,@NotBlank @Size(max=300) String reason){}
    public record CalculationResult(CommissionRun run,BigDecimal grossAmount,BigDecimal payableAmount,boolean capped){}
    public record Dashboard(long total,long pendingReview,long paid,BigDecimal calculatedAmount,BigDecimal clawbackAmount){}
}
