<?php

declare(strict_types=1);

function hashPasswordSecure(string $password): string
{
    return password_hash($password, PASSWORD_ARGON2ID);
}

function verifyPasswordSecure(
    string $password,
    string $hash
): bool {
    return password_verify($password, $hash);
}

function generateSecret(
    string $prefix
): string {
    return $prefix . bin2hex(random_bytes(32));
}

function hashSecret(
    string $secret
): string {
    return hash('sha256', $secret);
}

function generateProjectApiKey(
    string $environment
): array {
    if (!in_array($environment, ['test', 'live'], true)) {
        throw new InvalidArgumentException(
            'Invalid API key environment.'
        );
    }

    $secret = generateSecret(
        'sk_' . $environment . '_'
    );

    return [
        'secret' => $secret,
        'prefix' => substr($secret, 0, 16),
        'hash' => hashSecret($secret),
        'environment' => $environment
    ];
}
