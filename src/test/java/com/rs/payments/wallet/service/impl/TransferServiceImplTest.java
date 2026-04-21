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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer Service Tests")
class TransferServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private UUID fromWalletId;
    private UUID toWalletId;
    private Wallet fromWallet;
    private Wallet toWallet;

    @BeforeEach
    void setUp() {
        fromWalletId = UUID.randomUUID();
        toWalletId = UUID.randomUUID();

        fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);
        fromWallet.setBalance(BigDecimal.valueOf(200.00));

        toWallet = new Wallet();
        toWallet.setId(toWalletId);
        toWallet.setBalance(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("Should transfer funds atomically")
    void shouldTransferFundsAtomically() {
        // Given
        BigDecimal transferAmount = BigDecimal.valueOf(50.00);
        TransferRequest request = new TransferRequest(fromWalletId, toWalletId, transferAmount);

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        TransferResponse response = transferService.transfer(request);

        // Then
        assertEquals(BigDecimal.valueOf(150.00), response.getFromWalletBalance());
        assertEquals(BigDecimal.valueOf(150.00), response.getToWalletBalance());
        assertEquals(transferAmount, response.getAmount());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should create two transaction records (TRANSFER_OUT and TRANSFER_IN)")
    void shouldCreateTwoTransactionRecords() {
        // Given
        BigDecimal transferAmount = BigDecimal.valueOf(30.00);
        TransferRequest request = new TransferRequest(fromWalletId, toWalletId, transferAmount);

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        transferService.transfer(request);

        // Then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        
        var transactions = captor.getAllValues();
        assertEquals(2, transactions.size());
        
        // First transaction should be TRANSFER_OUT from source wallet
        assertEquals(TransactionType.TRANSFER_OUT, transactions.get(0).getType());
        assertEquals(fromWallet, transactions.get(0).getWallet());
        
        // Second transaction should be TRANSFER_IN to destination wallet
        assertEquals(TransactionType.TRANSFER_IN, transactions.get(1).getType());
        assertEquals(toWallet, transactions.get(1).getWallet());
    }

    @Test
    @DisplayName("Should throw exception when source wallet not found")
    void shouldThrowExceptionWhenSourceWalletNotFound() {
        // Given
        TransferRequest request = new TransferRequest(fromWalletId, toWalletId, BigDecimal.valueOf(50.00));
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> transferService.transfer(request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when destination wallet not found")
    void shouldThrowExceptionWhenDestinationWalletNotFound() {
        // Given
        TransferRequest request = new TransferRequest(fromWalletId, toWalletId, BigDecimal.valueOf(50.00));
        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> transferService.transfer(request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds and rollback")
    void shouldThrowExceptionWhenInsufficientFundsAndRollback() {
        // Given
        BigDecimal transferAmount = BigDecimal.valueOf(300.00); // More than source balance
        TransferRequest request = new TransferRequest(fromWalletId, toWalletId, transferAmount);

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> transferService.transfer(request));
        // Verify no saves occurred due to rollback
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}

