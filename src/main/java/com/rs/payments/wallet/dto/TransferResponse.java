package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for transfer transaction")
public class TransferResponse {
    @Schema(description = "From wallet ID")
    private UUID fromWalletId;

    @Schema(description = "To wallet ID")
    private UUID toWalletId;

    @Schema(description = "Amount transferred")
    private BigDecimal amount;

    @Schema(description = "From wallet balance after transfer")
    private BigDecimal fromWalletBalance;

    @Schema(description = "To wallet balance after transfer")
    private BigDecimal toWalletBalance;
}

