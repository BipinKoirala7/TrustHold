package com.trusthold.settlement.repository;

import com.trusthold.settlement.model.SettlementProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementRepository extends JpaRepository<SettlementProposal, UUID> {
}
