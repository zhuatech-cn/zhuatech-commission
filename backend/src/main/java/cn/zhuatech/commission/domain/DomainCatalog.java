/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.domain;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class DomainCatalog {
    private final Map<String, WorkflowAction> actions = new LinkedHashMap<>();
    public DomainCatalog() {
        actions.put("CALCULATE", new WorkflowAction("CALCULATE", "提交佣金计算", List.of("草稿"), "待复核", "OPERATOR"));
        actions.put("APPROVE", new WorkflowAction("APPROVE", "批准佣金结果", List.of("待复核"), "待发放", "ADMIN"));
        actions.put("PAY", new WorkflowAction("PAY", "确认佣金发放", List.of("待发放"), "已发放", "ADMIN"));
    }
    public String systemName() { return "知华科技企业佣金与销售激励管理系统"; }
    public String scene() { return "激励方案、适用资格、目标、交易归属、佣金计算、调整、审批、发放与追索"; }
    public String initialStatus() { return "草稿"; }
    public String partyLabel() { return "销售人员/渠道"; }
    public String amountLabel() { return "佣金金额"; }
    public String quantityLabel() { return "业绩笔数"; }
    public String dueLabel() { return "发放日期"; }
    public List<ModuleDefinition> modules() { return List.of(
            new ModuleDefinition("PLAN", "激励方案", "配置适用期间、产品、区域、角色、阶梯和封顶规则"),
            new ModuleDefinition("ELIGIBILITY", "资格管理", "维护人员、岗位、在职状态和方案适用资格"),
            new ModuleDefinition("TARGET", "目标管理", "分解收入、毛利、回款、产品和战略目标"),
            new ModuleDefinition("TRANSACTION", "业绩归属", "归集订单、回款、退款并处理拆分与归属争议"),
            new ModuleDefinition("CALCULATION", "佣金计算", "按规则版本计算阶梯、加速、奖金、封顶和扣减"),
            new ModuleDefinition("ADJUSTMENT", "调整申诉", "管理补发、扣回、人工调整、证据和申诉"),
            new ModuleDefinition("APPROVAL", "结果复核", "执行经理、财务、人力和合规多级复核"),
            new ModuleDefinition("PAYOUT", "发放管理", "生成发放批次并对接薪资、应付或渠道结算"),
            new ModuleDefinition("CLAWBACK_AUDIT", "追索审计", "对退单、坏账和违规交易执行追索并保留证据")
        ); }
    public Map<String, WorkflowAction> actions() { return Collections.unmodifiableMap(actions); }
    public record ModuleDefinition(String code,String name,String description) {}
    public record WorkflowAction(String code,String label,List<String> from,String to,String requiredRole) {}
}
