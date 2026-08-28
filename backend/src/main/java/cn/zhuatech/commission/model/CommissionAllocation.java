/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="commission_allocations",uniqueConstraints=@UniqueConstraint(columnNames={"runId","beneficiary"}))
public class CommissionAllocation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long runId;
    @Column(nullable=false,length=60) private String beneficiary;
    @Column(nullable=false,precision=7,scale=4) private BigDecimal creditPercent;
    @Column(nullable=false,precision=18,scale=2) private BigDecimal payableAmount;
    private LocalDateTime createdAt;
    protected CommissionAllocation(){}
    public CommissionAllocation(Long runId,String beneficiary,BigDecimal creditPercent,BigDecimal payableAmount){
        this.runId=runId;this.beneficiary=beneficiary;this.creditPercent=creditPercent;this.payableAmount=payableAmount;
    }
    @PrePersist void created(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public Long getRunId(){return runId;} public String getBeneficiary(){return beneficiary;}
    public BigDecimal getCreditPercent(){return creditPercent;} public BigDecimal getPayableAmount(){return payableAmount;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
