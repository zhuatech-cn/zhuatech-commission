/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="commission_runs",uniqueConstraints=@UniqueConstraint(columnNames="runNo"))
public class CommissionRun {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=40) private String runNo;
    @Column(nullable=false,length=40) private String planNo;
    @Column(nullable=false,length=60) private String beneficiary;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal eligibleRevenue;
    @Column(nullable=false,precision=8,scale=4) private BigDecimal baseRate;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal acceleratorThreshold;
    @Column(nullable=false,precision=8,scale=4) private BigDecimal acceleratorRate;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal capAmount;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal calculatedAmount=BigDecimal.ZERO;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal clawbackAmount=BigDecimal.ZERO;
    private int disputedItems;
    private boolean planApproved;
    private boolean dataLocked;
    private boolean complianceChecked;
    @Column(nullable=false,length=30) private String state;
    @Version private long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected CommissionRun(){}
    public CommissionRun(String runNo,String planNo,String beneficiary,BigDecimal eligibleRevenue,
            BigDecimal baseRate,BigDecimal acceleratorThreshold,BigDecimal acceleratorRate,BigDecimal capAmount,
            int disputedItems,boolean planApproved,boolean dataLocked,boolean complianceChecked){
        this.runNo=runNo;this.planNo=planNo;this.beneficiary=beneficiary;this.eligibleRevenue=eligibleRevenue;
        this.baseRate=baseRate;this.acceleratorThreshold=acceleratorThreshold;
        this.acceleratorRate=acceleratorRate;this.capAmount=capAmount;this.disputedItems=disputedItems;
        this.planApproved=planApproved;this.dataLocked=dataLocked;this.complianceChecked=complianceChecked;this.state="DRAFT";
    }
    @PrePersist void created(){createdAt=updatedAt=LocalDateTime.now();}
    @PreUpdate void updated(){updatedAt=LocalDateTime.now();}
    public void calculate(BigDecimal amount){calculatedAmount=amount;state="CALCULATED";}
    public void submit(){state="PENDING_REVIEW";} public void approve(){state="APPROVED";}
    public void pay(){state="PAID";}
    public void clawback(BigDecimal amount){clawbackAmount=clawbackAmount.add(amount);
        state=clawbackAmount.compareTo(calculatedAmount)>=0?"CLAWED_BACK":"PARTIAL_CLAWBACK";}

    public Long getId(){return id;} public String getRunNo(){return runNo;} public String getPlanNo(){return planNo;}
    public String getBeneficiary(){return beneficiary;} public BigDecimal getEligibleRevenue(){return eligibleRevenue;}
    public BigDecimal getBaseRate(){return baseRate;} public BigDecimal getAcceleratorThreshold(){return acceleratorThreshold;}
    public BigDecimal getAcceleratorRate(){return acceleratorRate;} public BigDecimal getCapAmount(){return capAmount;}
    public BigDecimal getCalculatedAmount(){return calculatedAmount;} public BigDecimal getClawbackAmount(){return clawbackAmount;}
    public int getDisputedItems(){return disputedItems;} public boolean isPlanApproved(){return planApproved;}
    public boolean isDataLocked(){return dataLocked;} public boolean isComplianceChecked(){return complianceChecked;}
    public String getState(){return state;} public long getVersion(){return version;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
