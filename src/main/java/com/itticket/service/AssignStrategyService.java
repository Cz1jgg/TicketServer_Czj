package com.itticket.service;

import com.itticket.common.BusinessException;
import com.itticket.entity.AssignStrategy;
import com.itticket.entity.SysUser;
import com.itticket.entity.TicketMain;
import com.itticket.repository.AssignStrategyRepository;
import com.itticket.repository.SysUserRepository;
import com.itticket.repository.TicketMainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignStrategyService {

    private final AssignStrategyRepository assignStrategyRepository;
    private final SysUserRepository sysUserRepository;
    private final TicketMainRepository ticketMainRepository;

    private static final String STRATEGY_ROUND_ROBIN = "ROUND_ROBIN";
    private static final String STRATEGY_LOAD_BALANCE = "LOAD_BALANCE";

    // 重点：修复 lambda 变量问题，改用数组包装，解决最终变量限制
    private final int[] roundRobinIndex = {0};

    public SysUser autoAssign(String strategyCode, TicketMain ticket) {
        List<SysUser> itSupportUsers = sysUserRepository.findByRoleRoleCodeAndDeleted("IT_SUPPORT", 0);

        if (itSupportUsers.isEmpty()) {
            throw BusinessException.of(400, "没有可用的IT支持人员");
        }

        if (STRATEGY_ROUND_ROBIN.equals(strategyCode)) {
            return assignByRoundRobin(itSupportUsers);
        } else if (STRATEGY_LOAD_BALANCE.equals(strategyCode)) {
            return assignByLoadBalance(itSupportUsers);
        } else {
            throw BusinessException.of(400, "未知的分配策略");
        }
    }

    private SysUser assignByRoundRobin(List<SysUser> users) {
        SysUser user = users.get(roundRobinIndex[0] % users.size());
        roundRobinIndex[0]++;
        return user;
    }

    private SysUser assignByLoadBalance(List<SysUser> users) {
        Pageable pageable = PageRequest.of(0, 1000);
        Map<Long, Long> ticketCountMap = ticketMainRepository
                .findByStatusAndDeleted("PROCESSING", 0, pageable)
                .stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId(), Collectors.counting()));

        SysUser bestUser = users.get(0);
        long minCount = Long.MAX_VALUE;

        for (SysUser user : users) {
            long count = ticketCountMap.getOrDefault(user.getId(), 0L);
            if (count < minCount) {
                minCount = count;
                bestUser = user;
            }
        }
        return bestUser;
    }

    @Transactional
    public AssignStrategy saveStrategy(AssignStrategy strategy) {
        if (assignStrategyRepository.findByStrategyCode(strategy.getStrategyCode()).isPresent()) {
            throw BusinessException.of(400, "策略编码已存在");
        }
        return assignStrategyRepository.save(strategy);
    }

    @Transactional
    public AssignStrategy updateStrategy(Long id, AssignStrategy strategy) {
        AssignStrategy existing = assignStrategyRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(404, "策略不存在"));

        existing.setStrategyName(strategy.getStrategyName());
        existing.setDescription(strategy.getDescription());
        existing.setIsActive(strategy.getIsActive());

        if (existing.getIsActive() == 1) {
            List<AssignStrategy> activeStrategies = assignStrategyRepository.findByIsActive(1);
            for (AssignStrategy active : activeStrategies) {
                if (!active.getId().equals(id)) {
                    active.setIsActive(0);
                    assignStrategyRepository.save(active);
                }
            }
        }
        return assignStrategyRepository.save(existing);
    }

    public List<AssignStrategy> getStrategies() {
        return assignStrategyRepository.findByDeleted(0);
    }

    public AssignStrategy getActiveStrategy() {
        List<AssignStrategy> strategies = assignStrategyRepository.findByIsActive(1);
        if (strategies.isEmpty()) {
            return null;
        }
        return strategies.get(0);
    }
}