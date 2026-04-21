package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfer Management", description = "APIs for managing peer-to-peer transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(
            summary = "Transfer funds between wallets",
            description = "Transfers funds from one wallet to another atomically.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = TransferResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid transfer amount or insufficient funds"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Source or destination wallet not found"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.transfer(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

