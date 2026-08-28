/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.commission.repository;

import cn.zhuatech.commission.model.CommissionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CommissionRunRepository extends JpaRepository<CommissionRun,Long> {
    Optional<CommissionRun> findByRunNo(String runNo);
    List<CommissionRun> findAllByOrderByUpdatedAtDesc();
    long countByState(String state);
}
