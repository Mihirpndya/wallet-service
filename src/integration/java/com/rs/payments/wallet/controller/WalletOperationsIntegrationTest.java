package com.rs.payments.wallet.controller;

import java.math.BigDecimal;
import java.util.UUID;
import com.rs.payments.wallet.BaseIntegrationTest;
import com.rs.payments.wallet.dto.CreateUserRequest;
import com.rs.payments.wallet.dto.CreateWalletRequest;
import com.rs.payments.wallet.dto.DepositRequest;
import com.rs.payments.wallet.dto.WithdrawRequest;
import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DisplayName("Wallet Operations Integration Tests")
class WalletOperationsIntegrationTest extends BaseIntegrationTest {

    private RestTemplate restTemplate = new RestTemplate();
    private UUID userId;
    private UUID walletId;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Create a user with unique username
        String uniqueUsername = "walletops_" + UUID.randomUUID().toString().substring(0, 8);
        CreateUserRequest userRequest = new CreateUserRequest(uniqueUsername, uniqueUsername + "@example.com");
        ResponseEntity<User> userResponse = restTemplate.postForEntity(
                baseUrl + "/users", userRequest, User.class);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        userId = userResponse.getBody().getId();

        // Create a wallet for the user
        CreateWalletRequest walletRequest = new CreateWalletRequest();
        walletRequest.setUserId(userId);
        ResponseEntity<Wallet> walletResponse = restTemplate.postForEntity(
                baseUrl + "/wallets", walletRequest, Wallet.class);
        assertThat(walletResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        walletId = walletResponse.getBody().getId();
    }

    @Test
    @DisplayName("Should deposit funds and update balance")
    void shouldDepositFunds() {
        // Given
        DepositRequest request = new DepositRequest(BigDecimal.valueOf(100.00));

        // When
        ResponseEntity<Wallet> response = restTemplate.postForEntity(
                baseUrl + "/wallets/" + walletId + "/deposit", request, Wallet.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBalance()).isNotNull();
        assertThat(response.getBody().getBalance().compareTo(BigDecimal.valueOf(100.00))).isEqualTo(0);
    }

    @Test
    @DisplayName("Should withdraw funds when sufficient balance")
    void shouldWithdrawFunds() {
        // Given - First deposit some funds
        DepositRequest depositRequest = new DepositRequest(BigDecimal.valueOf(100.00));
        restTemplate.postForEntity(
                baseUrl + "/wallets/" + walletId + "/deposit", depositRequest, Wallet.class);

        // When - Withdraw funds
        WithdrawRequest withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(50.00));
        ResponseEntity<Wallet> response = restTemplate.postForEntity(
                baseUrl + "/wallets/" + walletId + "/withdraw", withdrawRequest, Wallet.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBalance()).isNotNull();
        assertThat(response.getBody().getBalance().compareTo(BigDecimal.valueOf(50.00))).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return 400 when insufficient funds for withdrawal")
    void shouldReturnBadRequestWhenInsufficientFunds() {
        // Given - Don't deposit anything, balance is 0
        WithdrawRequest withdrawRequest = new WithdrawRequest(BigDecimal.valueOf(50.00));

        // When
        try {
            restTemplate.postForEntity(
                    baseUrl + "/wallets/" + walletId + "/withdraw", withdrawRequest, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Then
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("Should get wallet balance")
    void shouldGetWalletBalance() {
        // Given - Deposit some funds
        DepositRequest depositRequest = new DepositRequest(BigDecimal.valueOf(75.00));
        restTemplate.postForEntity(
                baseUrl + "/wallets/" + walletId + "/deposit", depositRequest, Wallet.class);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/wallets/" + walletId + "/balance", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("75.00");
    }

    @Test
    @DisplayName("Should transfer funds between wallets")
    void shouldTransferFunds() {
        // Given - Create another user and wallet
        String secondUsername = "walletops2_" + UUID.randomUUID().toString().substring(0, 8);
        CreateUserRequest secondUserRequest = new CreateUserRequest(secondUsername, secondUsername + "@example.com");
        ResponseEntity<User> secondUserResponse = restTemplate.postForEntity(
                baseUrl + "/users", secondUserRequest, User.class);
        UUID secondUserId = secondUserResponse.getBody().getId();

        CreateWalletRequest secondWalletRequest = new CreateWalletRequest();
        secondWalletRequest.setUserId(secondUserId);
        ResponseEntity<Wallet> secondWalletResponse = restTemplate.postForEntity(
                baseUrl + "/wallets", secondWalletRequest, Wallet.class);
        UUID secondWalletId = secondWalletResponse.getBody().getId();

        // Deposit in first wallet
        DepositRequest depositRequest = new DepositRequest(BigDecimal.valueOf(100.00));
        restTemplate.postForEntity(
                baseUrl + "/wallets/" + walletId + "/deposit", depositRequest, Wallet.class);

        // When - Transfer funds
        TransferRequest transferRequest = new TransferRequest(walletId, secondWalletId, BigDecimal.valueOf(50.00));
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/transfers", transferRequest, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}

