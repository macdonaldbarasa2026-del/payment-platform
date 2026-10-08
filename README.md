# Payment Platform

Developer-first payment infrastructure.

## Architecture

- PHP API
- Next.js Developer Dashboard
- C++ Payment Core
- Python Fraud Engine
- Supabase PostgreSQL
- Android developer dashboard

## Database

The API supports a Supabase PostgreSQL connection through:

```text
DATABASE_URL
```text
10.0.2.2 is only a local Android-emulator address. It is not an Internet URL.

## Health check

GET /health

## Production

The API can be deployed as a Docker web service.

After deployment, Android should use the public HTTPS API URL instead of:

http://10.0.2.2:8000

Never put database passwords or live API secrets in GitHub.
