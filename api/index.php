<?php

declare(strict_types=1);

header('Content-Type: application/json');
header('X-Powered-By: Payment-Platform');

require_once __DIR__ . '/config.php';
require_once __DIR__ . '/src/security.php';
require_once __DIR__ . '/src/validation.php';
require_once __DIR__ . '/src/api_keys.php';

function jsonResponse(
    array $data,
    int $status = 200
): never {
    http_response_code($status);

    echo json_encode(
        $data,
        JSON_UNESCAPED_SLASHES |
        JSON_UNESCAPED_UNICODE
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

    if (
        preg_match(
            '/^Bearer\s+(.+)$/i',
            trim($header),
            $matches
        )
    ) {
        return trim($matches[1]);
    }

    return null;
}

function authenticateApiKey(PDO $db): array
{
    $key = bearerToken();

    if ($key === null) {
        jsonResponse([
            'error' => 'missing_api_key',
            'message' => 'Authorization Bearer API key is required.',
            'request_id' => requestId()
        ], 401);
    }

    if (
        !preg_match(
            '/^sk_(test|live)_[A-Za-z0-9]+$/',
            $key
        )
    ) {
        jsonResponse([
            'error' => 'invalid_api_key',
            'message' => 'The API key format is invalid.',
            'request_id' => requestId()
        ], 401);
    }

    $hash = hashSecret($key);

    $stmt = $db->prepare(
        'SELECT
            id,
            project_id,
            environment,
            revoked
         FROM api_keys
         WHERE key_hash = :hash
         LIMIT 1'
    );

    $stmt->execute([
        ':hash' => $hash
    ]);

    $record = $stmt->fetch();

    if (!$record || $record['revoked']) {
        jsonResponse([
            'error' => 'invalid_api_key',
            'message' => 'The API key is invalid or revoked.',
            'request_id' => requestId()
        ], 401);
    }

    $stmt = $db->prepare(
        'UPDATE api_keys
         SET last_used_at = NOW()
         WHERE id = :id'
    );

    $stmt->execute([
        ':id' => $record['id']
    ]);

    return $record;
}

$path = parse_url(
    $_SERVER['REQUEST_URI'] ?? '/',
    PHP_URL_PATH
);

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';

try {

    $db = database();

    if (
        $path === '/health' &&
        $method === 'GET'
    ) {
        jsonResponse([
            'status' => 'ok',
            'service' => 'payment-api',
            'version' => '1.0.0',
            'database' => 'connected',
            'request_id' => requestId()
        ]);
    }

    if (
        $path === '/v1' &&
        $method === 'GET'
    ) {
        jsonResponse([
            'name' => 'Payment Platform API',
            'version' => '1.0.0',
            'status' => 'development'
        ]);
    }

    if (
        $path === '/v1/payments' &&
        $method === 'POST'
    ) {
        $auth = authenticateApiKey($db);
        $input = requestBody();

        $amount = $input['amount'] ?? null;
        $currency = strtoupper(
            trim((string)(
                $input['currency'] ?? 'KES'
            ))
        );
        $reference = trim(
            (string)($input['reference'] ?? '')
        );

        if (
            (!is_int($amount) &&
            !ctype_digit((string)$amount)) ||
            (int)$amount <= 0
        ) {
            jsonResponse([
                'error' => 'invalid_amount',
                'message' =>
                    'amount must be a positive integer.',
                'request_id' => requestId()
            ], 422);
        }

        if (
            !preg_match(
                '/^[A-Z]{3}$/',
                $currency
            )
        ) {
            jsonResponse([
                'error' => 'invalid_currency',
                'message' =>
                    'currency must be a three-letter ISO code.',
                'request_id' => requestId()
            ], 422);
        }

        if ($reference === '') {
            jsonResponse([
                'error' => 'missing_reference',
                'message' =>
                    'reference is required.',
                'request_id' => requestId()
            ], 422);
        }

        $stmt = $db->prepare(
            'INSERT INTO payments
            (
                project_id,
                reference,
                amount,
                currency,
                status
            )
            VALUES
            (
                :project_id,
                :reference,
                :amount,
                :currency,
                :status
            )
            RETURNING id, reference, amount, currency, status,
                      created_at'
        );

        $stmt->execute([
            ':project_id' => $auth['project_id'],
            ':reference' => $reference,
            ':amount' => (int)$amount,
            ':currency' => $currency,
            ':status' => 'pending'
        ]);

        $payment = $stmt->fetch();

        jsonResponse([
            'id' => 'pay_' . $payment['id'],
            'reference' => $payment['reference'],
            'amount' => (int)$payment['amount'],
            'currency' => $payment['currency'],
            'status' => $payment['status'],
            'environment' => $auth['environment'],
            'created_at' => $payment['created_at'],
            'request_id' => requestId()
        ], 201);
    }

    jsonResponse([
        'error' => 'endpoint_not_found',
        'message' =>
            'The requested endpoint does not exist.',
        'request_id' => requestId()
    ], 404);

} catch (InvalidArgumentException $e) {

    jsonResponse([
        'error' => 'invalid_request',
        'message' => $e->getMessage(),
        'request_id' => requestId()
    ], 422);

} catch (PDOException $e) {

    error_log($e->getMessage());

    jsonResponse([
        'error' => 'database_error',
        'message' => 'A database error occurred.',
        'request_id' => requestId()
    ], 500);

} catch (Throwable $e) {

    error_log($e->getMessage());

    jsonResponse([
        'error' => 'internal_error',
        'message' => 'An internal server error occurred.',
        'request_id' => requestId()
    ], 500);
}
