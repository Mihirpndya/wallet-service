package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.exception.InsufficientFundsException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import com.rs.payments.wallet.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Fetch source wallet
        Wallet fromWallet = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"));

        // Fetch destination wallet
        Wallet toWallet = walletRepository.findById(request.getToWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination wallet not found"));

        // Check if source wallet has sufficient funds
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Deduct from source wallet
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        fromWallet = walletRepository.save(fromWallet);

        // Add to destination wallet
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));
        toWallet = walletRepository.save(toWallet);

        // Create TRANSFER_OUT transaction
        Transaction outTransaction = new Transaction();
        outTransaction.setWallet(fromWallet);
        outTransaction.setAmount(request.getAmount());
        outTransaction.setType(TransactionType.TRANSFER_OUT);
        outTransaction.setTimestamp(LocalDateTime.now());
        outTransaction.setDescription("Transfer to " + request.getToWalletId());
        transactionRepository.save(outTransaction);

        // Create TRANSFER_IN transaction
        Transaction inTransaction = new Transaction();
        inTransaction.setWallet(toWallet);
        inTransaction.setAmount(request.getAmount());
        inTransaction.setType(TransactionType.TRANSFER_IN);
        inTransaction.setTimestamp(LocalDateTime.now());
        inTransaction.setDescription("Transfer from " + request.getFromWalletId());
        transactionRepository.save(inTransaction);

        return new TransferResponse(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount(),
                fromWallet.getBalance(),
                toWallet.getBalance()
        );
    }
}

