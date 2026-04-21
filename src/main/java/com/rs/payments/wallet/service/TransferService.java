package com.rs.payments.wallet.service;

import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.dto.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}

