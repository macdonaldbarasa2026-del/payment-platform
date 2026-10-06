<?php

declare(strict_types=1);

function validEmail(string $email): bool
{
    return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
}

function validPassword(string $password): bool
{
    return strlen($password) >= 8;
}

function requiredString(
    array $data,
    string $field
): string {
    $value = trim((string)($data[$field] ?? ''));

    if ($value === '') {
        throw new InvalidArgumentException(
            $field . ' is required.'
        );
    }

    return $value;
}
