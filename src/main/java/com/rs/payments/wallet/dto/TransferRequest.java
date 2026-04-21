package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to transfer funds between wallets")
public class TransferRequest {
    @NotNull(message = "From wallet ID cannot be null")
    @Schema(description = "Source wallet ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID fromWalletId;

    @NotNull(message = "To wallet ID cannot be null")
    @Schema(description = "Destination wallet ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID toWalletId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to transfer", example = "100.00")
    private BigDecimal amount;
}

