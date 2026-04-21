package com.rs.payments.wallet.service;

import java.util.UUID;
import com.rs.payments.wallet.dto.DepositRequest;
import com.rs.payments.wallet.model.Wallet;

public interface WalletService {
    Wallet createWalletForUser(UUID userId);
    Wallet deposit(UUID walletId, DepositRequest request);
}