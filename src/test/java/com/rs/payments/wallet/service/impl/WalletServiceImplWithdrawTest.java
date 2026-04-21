package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.WithdrawRequest;
import com.rs.payments.wallet.exception.InsufficientFundsException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
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
@DisplayName("Wallet Service Withdrawal Tests")
class WalletServiceImplWithdrawTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID walletId;
    private Wallet wallet;
    private User user;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100.00));

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setWallet(wallet);
        wallet.setUser(user);
    }

    @Test
    @DisplayName("Should withdraw funds and update balance atomically")
    void shouldWithdrawFundsAndUpdateBalance() {
        // Given
        BigDecimal withdrawAmount = BigDecimal.valueOf(30.00);
        WithdrawRequest request = new WithdrawRequest(withdrawAmount);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        Wallet result = walletService.withdraw(walletId, request);

        // Then
        assertEquals(BigDecimal.valueOf(70.00), result.getBalance());
        verify(walletRepository, times(1)).findById(walletId);
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        // Given
        BigDecimal withdrawAmount = BigDecimal.valueOf(150.00); // More than balance
        WithdrawRequest request = new WithdrawRequest(withdrawAmount);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> walletService.withdraw(walletId, request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not allow balance to go negative")
    void shouldNotAllowNegativeBalance() {
        // Given
        BigDecimal withdrawAmount = BigDecimal.valueOf(100.01);
        WithdrawRequest request = new WithdrawRequest(withdrawAmount);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> walletService.withdraw(walletId, request));
    }

    @Test
    @DisplayName("Should create transaction record with WITHDRAWAL type")
    void shouldCreateTransactionWithWithdrawalType() {
        // Given
        BigDecimal withdrawAmount = BigDecimal.valueOf(25.00);
        WithdrawRequest request = new WithdrawRequest(withdrawAmount);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        walletService.withdraw(walletId, request);

        // Then
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction transaction = captor.getValue();

        assertEquals(TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(withdrawAmount, transaction.getAmount());
        assertEquals(wallet, transaction.getWallet());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        // Given
        WithdrawRequest request = new WithdrawRequest(BigDecimal.valueOf(50.00));
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.withdraw(walletId, request));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}

