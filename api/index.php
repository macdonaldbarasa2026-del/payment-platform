<?php

declare(strict_types=1);

header('Content-Type: application/json');
header('X-Powered-By: Payment-Platform');

function jsonResponse(array $data, int $status = 200): never
{
    http_response_code($status);

    echo json_encode(
        $data,
        JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE
    );

    exit;
}

function requestId(): string
{
    return 'req_' . bin2hex(random_bytes(12));
}

function requestBody(): array
{
    $raw = file_get_contents('php://input');

    if ($raw === false || trim($raw) === '') {
        return [];
    }

    $data = json_decode($raw, true);

    if (!is_array($data)) {
        jsonResponse([
            'error' => 'invalid_json',
            'message' => 'Request body must contain valid JSON.',
            'request_id' => requestId()
        ], 400);
    }

    return $data;
}

function bearerToken(): ?string
{
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';

    if (preg_match('/^Bearer\s+(.+)$/i', trim($header), $matches)) {
        return trim($matches[1]);
    }

    return null;
}

function hashApiKey(string $key): string
{
    return hash('sha256', $key);
}

function generateApiKey(string $environment): array
{
    $secret = bin2hex(random_bytes(32));

    $key = 'sk_' . $environment . '_' . $secret;

    return [
        'key' => $key,
        'prefix' => substr($key, 0, 12),
        'hash' => hashApiKey($key)
    ];
}

function authenticateApiKey(): array
{
    $key = bearerToken();

    if ($key === null) {
        jsonResponse([
            'error' => 'missing_api_key',
            'message' => 'Authorization Bearer API key is required.',
            'request_id' => requestId()
        ], 401);
    }

    if (!preg_match('/^sk_(test|live)_[A-Za-z0-9]+$/', $key)) {
        jsonResponse([
            'error' => 'invalid_api_key',
            'message' => 'The API key format is invalid.',
            'request_id' => requestId()
        ], 401);
    }

    return [
        'environment' => str_starts_with($key, 'sk_test_')
            ? 'test'
            : 'live',
        'hash' => hashApiKey($key)
    ];
}

$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

if ($path === '/health' && $method === 'GET') {
    jsonResponse([
        'status' => 'ok',
        'service' => 'payment-api',
        'version' => '1.0.0',
        'request_id' => requestId()
    ]);
}

if ($path === '/v1' && $method === 'GET') {
    jsonResponse([
        'name' => 'Payment Platform API',
        'version' => '1.0.0',
        'status' => 'development'
    ]);
}

if ($path === '/v1/payments' && $method === 'POST') {

    $auth = authenticateApiKey();
    $input = requestBody();

    $amount = $input['amount'] ?? null;
    $currency = strtoupper(trim((string)($input['currency'] ?? 'KES')));
    $reference = trim((string)($input['reference'] ?? ''));

    if (
        (!is_int($amount) && !ctype_digit((string)$amount))
        || (int)$amount <= 0
    ) {
        jsonResponse([
            'error' => 'invalid_amount',
            'message' => 'amount must be a positive integer in the smallest currency unit.',
            'request_id' => requestId()
        ], 422);
    }

    if (!preg_match('/^[A-Z]{3}$/', $currency)) {
        jsonResponse([
            'error' => 'invalid_currency',
            'message' => 'currency must be a three-letter ISO currency code.',
            'request_id' => requestId()
        ], 422);
    }

    if ($reference === '') {
        jsonResponse([
            'error' => 'missing_reference',
            'message' => 'reference is required.',
            'request_id' => requestId()
        ], 422);
    }

    jsonResponse([
        'id' => 'pay_' . bin2hex(random_bytes(12)),
        'reference' => $reference,
        'amount' => (int)$amount,
        'currency' => $currency,
        'status' => 'pending',
        'environment' => $auth['environment'],
        'request_id' => requestId()
    ], 201);
}

jsonResponse([
    'error' => 'endpoint_not_found',
    'message' => 'The requested endpoint does not exist.',
    'request_id' => requestId()
], 404);
