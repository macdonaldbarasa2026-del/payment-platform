<?php

declare(strict_types=1);

header('Content-Type: application/json');

function response(array $data, int $status = 200): never
{
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_SLASHES);
    exit;
}

function body(): array
{
    $raw = file_get_contents('php://input');

    if (!$raw) {
        return [];
    }

    $data = json_decode($raw, true);

    return is_array($data) ? $data : [];
}

function requestId(): string
{
    return 'req_' . bin2hex(random_bytes(12));
}

$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

if ($path === '/health') {
    response([
        'status' => 'ok',
        'service' => 'payment-api',
        'request_id' => requestId()
    ]);
}

if ($path === '/v1') {
    response([
        'name' => 'Payment Platform API',
        'version' => '1.0.0',
        'status' => 'development'
    ]);
}

if ($path === '/v1/payments' && $method === 'POST') {

    $input = body();

    $amount = $input['amount'] ?? null;
    $currency = strtoupper((string)($input['currency'] ?? 'KES'));
    $reference = trim((string)($input['reference'] ?? ''));

    if (!is_int($amount) && !ctype_digit((string)$amount)) {
        response([
            'error' => 'invalid_amount',
            'message' => 'amount must be a positive integer in the smallest currency unit'
        ], 422);
    }

    $amount = (int)$amount;

    if ($amount <= 0) {
        response([
            'error' => 'invalid_amount'
        ], 422);
    }

    if ($reference === '') {
        response([
            'error' => 'missing_reference'
        ], 422);
    }

    response([
        'id' => 'pay_' . bin2hex(random_bytes(12)),
        'reference' => $reference,
        'amount' => $amount,
        'currency' => $currency,
        'status' => 'pending',
        'request_id' => requestId()
    ], 201);
}

response([
    'error' => 'endpoint_not_found',
    'request_id' => requestId()
], 404);
