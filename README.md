# Payment Platform

Developer-first payment infrastructure.

## Services

- API Gateway
- Next.js Developer Dashboard
- C++ Payment Core
- Python Fraud Engine
- PostgreSQL

## Developer workflow

1. Create an account
2. Create a project
3. Generate API keys
4. Integrate the API
5. Create test payments
6. Configure webhooks
7. Generate live keys after verification

## API keys

Test:

sk_test_...

Live:

sk_live_...

API secrets are stored as cryptographic hashes and are not stored in plaintext.

## CI/CD

GitHub Actions builds and tests the platform.

Termux is used for source-code editing and Git operations, not compilation.
