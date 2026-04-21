package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.DepositRequest;
import com.rs.payments.wallet.dto.WithdrawRequest;
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
@DisplayName("Wallet Service Deposit and Withdrawal Tests")
class WalletServiceImplDepositWithdrawTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should deposit funds and update balance")
    void shouldDepositFunds() {
        // Given
        BigDecimal depositAmount = BigDecimal.valueOf(100.00);
        DepositRequest request = new DepositRequest(depositAmount);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        Wallet result = walletService.deposit(walletId, request);

        // Then
        assertEquals(BigDecimal.valueOf(100.00), result.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should create transaction record for deposit")
    void shouldCreateDepositTransaction() {
        // Given
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(50.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        walletService.deposit(walletId, request);

        // Then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        
        Transaction transaction = captor.getValue();
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(BigDecimal.valueOf(50.00), transaction.getAmount());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found for deposit")
    void shouldThrowExceptionWhenWalletNotFoundForDeposit() {
        // Given
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(50.00));
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.deposit(walletId, request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should withdraw funds successfully")
    void shouldWithdrawFunds() {
        // Given
        wallet.setBalance(BigDecimal.valueOf(100.00));
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(50.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        Wallet result = walletService.withdraw(walletId, request);

        // Then
        assertEquals(BigDecimal.valueOf(50.00), result.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should create transaction record for withdrawal")
    void shouldCreateWithdrawalTransaction() {
        // Given
        wallet.setBalance(BigDecimal.valueOf(100.00));
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(30.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        walletService.withdraw(walletId, request);

        // Then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        
        Transaction transaction = captor.getValue();
        assertEquals(TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(BigDecimal.valueOf(30.00), transaction.getAmount());
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        // Given
        wallet.setBalance(BigDecimal.valueOf(30.00));
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> walletService.withdraw(walletId, request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found for withdrawal")
    void shouldThrowExceptionWhenWalletNotFoundForWithdraw() {
        // Given
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(50.00));
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.withdraw(walletId, request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return correct balance")
    void shouldReturnCorrectBalance() {
        // Given
        wallet.setBalance(BigDecimal.valueOf(75.50));
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When
        BigDecimal balance = walletService.getBalance(walletId);

        // Then
        assertEquals(BigDecimal.valueOf(75.50), balance);
        verify(walletRepository, times(1)).findById(walletId);
    }

    @Test
    @DisplayName("Should throw exception when wallet not found for balance inquiry")
    void shouldThrowExceptionWhenWalletNotFoundForBalance() {
        // Given
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.getBalance(walletId));
    }
}

