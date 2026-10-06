<?php

declare(strict_types=1);

header('Content-Type: application/json');

$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

if ($path === '/health') {
    echo json_encode([
        'status' => 'ok',
        'service' => 'payment-api'
    ]);
    exit;
}

if ($path === '/v1') {
    echo json_encode([
        'name' => 'Payment Platform API',
        'version' => '1.0.0'
    ]);
    exit;
}

http_response_code(404);

echo json_encode([
    'error' => 'endpoint_not_found'
]);
