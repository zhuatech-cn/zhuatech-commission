/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.service;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import java.util.*;
@Service public class DomainDecisionService {
 public DecisionResult assess(DecisionRequest request) { int score=100;List<String> actions=new ArrayList<>();if(request.calculatedCommission()>request.capAmount()){score-=35;actions.add("按方案封顶规则复核佣金");}if(request.disputedItems()>0){score-=Math.min(30,request.disputedItems()*5);actions.add("处理佣金归属争议");}if(!request.planApproved()){score-=60;actions.add("完成激励方案事前审批");}if(!request.dataLocked()){score-=30;actions.add("锁定业绩数据和计算版本");}if(!request.complianceChecked()){score-=50;actions.add("完成合规与异常交易检查");}if(request.clawbackPending()){score-=35;actions.add("完成退款或坏账佣金追索");}double rate=request.eligibleRevenue()==0?0:request.calculatedCommission()*100d/request.eligibleRevenue();return result(score,actions,"APPROVE_PAYOUT","MANUAL_REVIEW","BLOCKED",Map.of("commissionRate",Math.round(rate*100)/100d,"capAmount",request.capAmount(),"disputedItems",request.disputedItems(),"clawbackPending",request.clawbackPending())); }
 private DecisionResult result(int raw,List<String> actions,String good,String warn,String bad,Map<String,Object> metrics) { int score=Math.max(0,Math.min(100,raw));String decision=score>=80?good:score>=50?warn:bad;return new DecisionResult(decision,score,metrics,List.copyOf(actions)); }
 private DecisionResult riskResult(int raw,List<String> actions,String good,String warn,String bad,Map<String,Object> metrics) { int score=Math.max(0,Math.min(100,raw));String decision=score>=70?bad:score>=40?warn:good;return new DecisionResult(decision,score,metrics,List.copyOf(actions)); }
 public record DecisionRequest(
        @NotBlank String planNo,
        @PositiveOrZero double eligibleRevenue,
        @PositiveOrZero double calculatedCommission,
        @PositiveOrZero double capAmount,
        @PositiveOrZero int disputedItems,
        boolean planApproved,
        boolean dataLocked,
        boolean complianceChecked,
        boolean clawbackPending) {}
 public record DecisionResult(String decision,int score,Map<String,Object> metrics,List<String> actions) {}
}
