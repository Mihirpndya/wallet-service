package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.exception.DuplicateWalletException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("Should create wallet for existing user")
    void shouldCreateWalletForExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // The service saves the user, which cascades to wallet. 
        // We mock save to return the user.
        when(userRepository.save(user)).thenReturn(user);

        // When
        Wallet result = walletService.createWalletForUser(userId);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getBalance());
        
        // Verify interactions
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user already has wallet")
    void shouldThrowExceptionWhenUserAlreadyHasWallet() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Wallet existingWallet = new Wallet();
        existingWallet.setId(UUID.randomUUID());
        user.setWallet(existingWallet);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(DuplicateWalletException.class, () -> walletService.createWalletForUser(userId));
        verify(userRepository, never()).save(any());
    }
}
