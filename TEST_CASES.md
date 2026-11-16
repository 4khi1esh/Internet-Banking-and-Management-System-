# Test cases

| No. | Test | Expected result |
|---|---|---|
| 1 | Register with valid name, unique email and password | Customer and zero-balance account created |
| 2 | Register with an already-used email | Database rejects duplicate email |
| 3 | Login with correct credentials | Dashboard opens |
| 4 | Login with incorrect credentials | Error message appears |
| 5 | Deposit a positive amount | Balance increases and transaction is recorded |
| 6 | Withdraw less than or equal to balance | Balance decreases and transaction is recorded |
| 7 | Withdraw more than balance | Insufficient-balance error; balance unchanged |
| 8 | Transfer to another valid account | Both balances update and transfer is recorded |
| 9 | Transfer to an invalid account | Error; balances unchanged |
| 10 | View transaction history | Transactions for the logged-in account appear |
