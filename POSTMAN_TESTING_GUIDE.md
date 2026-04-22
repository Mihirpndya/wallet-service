# Postman Testing Guide - Wallet Service API

## Quick Start

### Prerequisites
- Postman installed (download from https://www.postman.com/downloads/)
- Wallet Service API running at `http://localhost:8080`
- Import the Postman collection: `Wallet_Service_Postman.json`

---

## STEP-BY-STEP TESTING GUIDE

### STEP 1: Import Collection into Postman

1. Open Postman
2. Click **File** → **Import**
3. Select file: `Wallet_Service_Postman.json`
4. Click **Import**
5. New collection "Wallet Service API" will appear in left panel

---

### STEP 2: Set Environment Variables

1. In Postman, click **Environment** (top-right)
2. Click **Create New** or **Edit** 
3. Add these variables:
   - **base_url**: `http://localhost:8080`
   - **user_id_alice**: Leave blank (will be filled after creating user)
   - **user_id_bob**: Leave blank (will be filled after creating user)
   - **wallet_id_alice**: Leave blank (will be filled after creating wallet)
   - **wallet_id_bob**: Leave blank (will be filled after creating wallet)

4. Click **Save**

---

## TEST WORKFLOW

### Test 1: Create User - Alice ✓

**Endpoint**: `POST /users`

1. In Postman left panel, expand collection
2. Click **1. USER MANAGEMENT** → **POST Create User - Alice**
3. Verify URL: `http://localhost:8080/users`
4. Verify Method: **POST**
5. Click **Send**

**Expected Response (201 Created)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "alice",
  "email": "alice@example.com",
  "wallet": null
}
```

**IMPORTANT**: Copy the `id` value and save it

---

### Test 2: Create User - Bob ✓

1. Click **POST Create User - Bob**
2. Click **Send**

**Expected Response (201 Created)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "username": "bob",
  "email": "bob@example.com",
  "wallet": null
}
```

**IMPORTANT**: Copy this `id` value as well

---

### Test 3: Create Wallet for Alice ✓

1. Click **2. WALLET MANAGEMENT** → **POST Create Wallet - Alice**
2. Replace `<USER_ID_ALICE>` in request body with Alice's user ID from Test 1
3. Body should look like:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```
4. Click **Send**

**Expected Response (201 Created)**:
```json
{
  "id": "wallet-alice-id-here",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "alice",
    "email": "alice@example.com"
  },
  "balance": "0.00"
}
```

**IMPORTANT**: Save the wallet `id`

---

### Test 4: Create Wallet for Bob ✓

1. Click **POST Create Wallet - Bob**
2. Replace `<USER_ID_BOB>` with Bob's user ID
3. Click **Send**

**Expected Response (201 Created)**:
```json
{
  "id": "wallet-bob-id-here",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "bob",
    "email": "bob@example.com"
  },
  "balance": "0.00"
}
```

**IMPORTANT**: Save Bob's wallet `id`

---

### Test 5: Deposit into Alice's Wallet ✓

1. Click **POST Deposit Alice 500.00**
2. Update URL path with Alice's wallet ID:
   - Change: `<WALLET_ID_ALICE>` → actual wallet ID
   - Example: `http://localhost:8080/wallets/550e8400-e29b-41d4-a716-446655440100/deposit`
3. Verify body:
```json
{
  "amount": "500.00"
}
```
4. Click **Send**

**Expected Response (200 OK)**:
```json
{
  "id": "wallet-id-alice",
  "user": {...},
  "balance": "500.00"
}
```

✅ **Verify balance is now 500.00**

---

### Test 6: Check Alice's Balance ✓

1. Click **GET Alice Balance**
2. Update URL: `{{base_url}}/wallets/<WALLET_ID_ALICE>/balance`
3. Click **Send**

**Expected Response (200 OK)**:
```json
{
  "balance": "500.00"
}
```

---

### Test 7: Withdraw from Alice's Wallet ✓

1. Click **POST Withdraw Alice 100.00**
2. Update wallet ID in URL
3. Verify body:
```json
{
  "amount": "100.00"
}
```
4. Click **Send**

**Expected Response (200 OK)**:
```json
{
  "id": "wallet-id-alice",
  "user": {...},
  "balance": "400.00"
}
```

✅ **Verify balance decreased to 400.00** (500 - 100)

---

### Test 8: Deposit into Bob's Wallet ✓

1. Create new request or modify POST Deposit Alice
2. **Method**: POST
3. **URL**: `{{base_url}}/wallets/<WALLET_ID_BOB>/deposit`
4. **Body**:
```json
{
  "amount": "200.00"
}
```
5. Click **Send**

**Expected Response (200 OK)**:
```json
{
  "balance": "200.00"
}
```

---

### Test 9: Transfer from Alice to Bob ✓

1. Click **3. TRANSFERS** → **POST Transfer Alice to Bob 150.00**
2. Update request body with correct wallet IDs:
```json
{
  "fromWalletId": "wallet-alice-id-here",
  "toWalletId": "wallet-bob-id-here",
  "amount": "150.00"
}
```
3. Click **Send**

**Expected Response (200 OK)**:
```json
{
  "fromWalletId": "wallet-alice-id-here",
  "toWalletId": "wallet-bob-id-here",
  "amount": "150.00",
  "fromWalletBalance": "250.00",
  "toWalletBalance": "350.00"
}
```

✅ **Verify both balances updated atomically**:
- Alice: 400.00 - 150.00 = 250.00 ✓
- Bob: 200.00 + 150.00 = 350.00 ✓

---

### Test 10: Verify Final Balances ✓

1. Click **GET Alice Balance After Transfer**
2. Update wallet ID
3. Click **Send**

**Expected**: `{"balance": "250.00"}`

4. Click **GET Bob Balance After Transfer**
5. Update wallet ID
6. Click **Send**

**Expected**: `{"balance": "350.00"}`

---

## ERROR SCENARIO TESTING

### Test Error 1: Duplicate User (409 Conflict)

1. Create new POST request
2. **URL**: `{{base_url}}/users`
3. **Body**:
```json
{
  "username": "alice",
  "email": "different@example.com"
}
```
4. Click **Send**

**Expected Response (409 Conflict)**:
```json
{
  "status": 409,
  "message": "User already exists",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/users"
}
```

---

### Test Error 2: Duplicate Wallet (400 Bad Request)

1. Create new POST request
2. **URL**: `{{base_url}}/wallets`
3. **Body**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```
(Use Alice's ID - she already has a wallet)

4. Click **Send**

**Expected Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "User already has a wallet",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/wallets"
}
```

---

### Test Error 3: Insufficient Funds (400 Bad Request)

1. Create new POST request
2. **Method**: POST
3. **URL**: `{{base_url}}/wallets/<WALLET_ID_ALICE>/withdraw`
4. **Body**:
```json
{
  "amount": "99999.99"
}
```
5. Click **Send**

**Expected Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Insufficient funds for withdrawal",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/wallets/.../withdraw"
}
```

---

### Test Error 4: Invalid Amount - Zero (400 Bad Request)

1. Create new POST request
2. **URL**: `{{base_url}}/wallets/<WALLET_ID_ALICE>/deposit`
3. **Body**:
```json
{
  "amount": "0"
}
```
4. Click **Send**

**Expected Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Amount must be greater than 0",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/wallets/.../deposit"
}
```

---

### Test Error 5: Non-existent Wallet (404 Not Found)

1. Create new GET request
2. **URL**: `{{base_url}}/wallets/00000000-0000-0000-0000-000000000000/balance`
3. Click **Send**

**Expected Response (404 Not Found)**:
```json
{
  "status": 404,
  "message": "Wallet not found",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/wallets/.../balance"
}
```

---

### Test Error 6: Transfer Insufficient Funds (400 Bad Request)

1. Create new POST request
2. **URL**: `{{base_url}}/transfers`
3. **Body**:
```json
{
  "fromWalletId": "wallet-alice-id",
  "toWalletId": "wallet-bob-id",
  "amount": "99999.99"
}
```
4. Click **Send**

**Expected Response (400 Bad Request)**:
```json
{
  "status": 400,
  "message": "Insufficient funds for transfer",
  "timestamp": "2026-04-22T10:00:00Z",
  "path": "/transfers"
}
```

✅ **Note**: Source wallet balance should NOT change (atomic rollback)

---

## POSTMAN TIPS & TRICKS

### 1. Save User/Wallet IDs Automatically

Add test script to automatically capture IDs:

1. Click on request → **Tests** tab
2. Add script:
```javascript
if (pm.response.code === 201 || pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.id && jsonData.username) {
        pm.environment.set("user_id_" + jsonData.username, jsonData.id);
        console.log("Saved: " + jsonData.username + " ID: " + jsonData.id);
    }
    if (jsonData.id && jsonData.balance !== undefined) {
        pm.environment.set("wallet_id", jsonData.id);
        console.log("Saved: Wallet ID: " + jsonData.id);
    }
}
```

### 2. Use Pre-request Scripts

For dynamic request bodies:

1. Click **Pre-request Script**
2. Generate random username:
```javascript
var randomId = Math.random().toString(36).substring(7);
pm.environment.set("random_username", "user_" + randomId);
```

### 3. Check Response Times

Postman shows response time in bottom right. Monitor for performance.

### 4. Use Collections Runner

Test entire collection in sequence:

1. Right-click collection name
2. Click **Run collection**
3. Click **Run Wallet Service API**
4. Tests run sequentially with results

---

## STATUS CODE REFERENCE

| Code | Meaning | When to Expect |
|------|---------|----------------|
| **200** | OK | Successful operation (deposit, withdraw, get balance, transfer) |
| **201** | Created | User or wallet created successfully |
| **400** | Bad Request | Invalid input, insufficient funds, duplicate wallet |
| **404** | Not Found | User or wallet not found |
| **409** | Conflict | Duplicate user |

---

## TESTING CHECKLIST

- [ ] Create User 1
- [ ] Create User 2
- [ ] Create Wallet 1
- [ ] Create Wallet 2
- [ ] Deposit funds into Wallet 1
- [ ] Check balance after deposit
- [ ] Withdraw funds from Wallet 1
- [ ] Check balance after withdrawal
- [ ] Transfer between wallets
- [ ] Verify both balances updated
- [ ] Test duplicate user (expect 409)
- [ ] Test duplicate wallet (expect 400)
- [ ] Test insufficient funds withdrawal (expect 400)
- [ ] Test insufficient funds transfer (expect 400)
- [ ] Test invalid amount zero (expect 400)
- [ ] Test non-existent wallet (expect 404)

---

## COMMON ISSUES & SOLUTIONS

### Issue 1: "URL cannot be empty"
**Solution**: Replace placeholders like `<WALLET_ID_ALICE>` with actual IDs

### Issue 2: "401 Unauthorized"
**Solution**: No authentication required - just ensure API is running

### Issue 3: "Connection refused"
**Solution**: Ensure API running at `http://localhost:8080`
```bash
./gradlew bootRun
# or
docker-compose up
```

### Issue 4: "Invalid UUID string"
**Solution**: Ensure wallet/user IDs are in correct UUID format

### Issue 5: "Amount must be greater than 0"
**Solution**: Use amounts like "100.00" (not "0" or negative)

---

## WORKFLOW SUMMARY

```
1. Create Alice (POST /users)
   ↓
2. Create Bob (POST /users)
   ↓
3. Create Alice's Wallet (POST /wallets)
   ↓
4. Create Bob's Wallet (POST /wallets)
   ↓
5. Deposit 500 to Alice (POST /wallets/{id}/deposit)
   ↓
6. Deposit 200 to Bob (POST /wallets/{id}/deposit)
   ↓
7. Withdraw 100 from Alice (POST /wallets/{id}/withdraw)
   ↓
8. Transfer 150 Alice → Bob (POST /transfers)
   ↓
9. Verify Final Balances (GET /wallets/{id}/balance)
   Alice: 250.00 ✓
   Bob: 350.00 ✓
```

---

## NEXT STEPS

1. **Import Collection**: Use `Wallet_Service_Postman.json`
2. **Update Environment**: Set `base_url = http://localhost:8080`
3. **Follow Tests in Order**: Tests 1-10 in sequence
4. **Test Error Cases**: Run error tests to verify error handling
5. **Experiment**: Create your own test requests

---

## Quick Access URLs

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Base**: http://localhost:8080
- **Users**: http://localhost:8080/users
- **Wallets**: http://localhost:8080/wallets
- **Transfers**: http://localhost:8080/transfers

**Happy Testing!** 🚀


