<?php

declare(strict_types=1);

function createSessionToken(
    string $clientId
): string {
    $payload = [
        'sub' => $clientId,
        'iat' => time(),
        'exp' => time() + 86400
    ];

    $encoded = base64_encode(
        json_encode($payload)
    );

    $signature = hash_hmac(
        'sha256',
        $encoded,
        getenv('APP_SECRET') ?: 'development-secret'
    );

    return rtrim(
        strtr(
            base64_encode(
                $encoded . '.' . $signature
            ),
            '+/',
            '-_'
        ),
        '='
    );
}

function clientFromSession(
    PDO $db
): ?array {
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';

    if (
        !preg_match(
            '/^Bearer\s+(.+)$/i',
            trim($header),
            $matches
        )
    ) {
        return null;
    }

    $token = trim($matches[1]);

    $decoded = base64_decode(
        strtr($token, '-_', '+/')
    );

    if ($decoded === false) {
        return null;
    }

    $parts = explode('.', $decoded, 2);

    if (count($parts) !== 2) {
        return null;
    }

    [$encoded, $signature] = $parts;

    $expected = hash_hmac(
        'sha256',
        $encoded,
        getenv('APP_SECRET') ?: 'development-secret'
    );

    if (!hash_equals($expected, $signature)) {
        return null;
    }

    $payload = json_decode(
        base64_decode($encoded),
        true
    );

    if (
        !is_array($payload) ||
        empty($payload['sub']) ||
        empty($payload['exp']) ||
        $payload['exp'] < time()
    ) {
        return null;
    }

    $stmt = $db->prepare(
        'SELECT id, name, email, created_at
         FROM clients
         WHERE id = :id
         LIMIT 1'
    );

    $stmt->execute([
        ':id' => $payload['sub']
    ]);

    $client = $stmt->fetch();

    return $client ?: null;
}
