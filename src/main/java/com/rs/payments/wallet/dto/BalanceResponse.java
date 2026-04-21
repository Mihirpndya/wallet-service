package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response with wallet balance")
public class BalanceResponse {
    @Schema(description = "Current balance in the wallet", example = "100.50")
    private BigDecimal balance;
}

