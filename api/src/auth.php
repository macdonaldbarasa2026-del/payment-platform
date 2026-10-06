<?php

declare(strict_types=1);

function registerClient(
    PDO $db,
    string $name,
    string $email,
    string $password
): array {
    $name = trim($name);
    $email = strtolower(trim($email));

    if ($name === '') {
        throw new InvalidArgumentException('name is required.');
    }

    if (!validEmail($email)) {
        throw new InvalidArgumentException('A valid email is required.');
    }

    if (!validPassword($password)) {
        throw new InvalidArgumentException(
            'Password must contain at least 8 characters.'
        );
    }

    $passwordHash = hashPasswordSecure($password);

    try {
        $stmt = $db->prepare(
            'INSERT INTO clients
             (name, email, password_hash)
             VALUES
             (:name, :email, :password_hash)
             RETURNING id, name, email, created_at'
        );

        $stmt->execute([
            ':name' => $name,
            ':email' => $email,
            ':password_hash' => $passwordHash
        ]);

        $client = $stmt->fetch();

        if (!$client) {
            throw new RuntimeException(
                'Unable to create account.'
            );
        }

        return $client;

    } catch (PDOException $e) {
        if ($e->getCode() === '23505') {
            throw new InvalidArgumentException(
                'An account with this email already exists.'
            );
        }

        throw $e;
    }
}

function authenticateClient(
    PDO $db,
    string $email,
    string $password
): array {
    $email = strtolower(trim($email));

    $stmt = $db->prepare(
        'SELECT id, name, email, password_hash, created_at
         FROM clients
         WHERE email = :email
         LIMIT 1'
    );

    $stmt->execute([
        ':email' => $email
    ]);

    $client = $stmt->fetch();

    if (
        !$client ||
        !verifyPasswordSecure(
            $password,
            $client['password_hash']
        )
    ) {
        throw new InvalidArgumentException(
            'Invalid email or password.'
        );
    }

    unset($client['password_hash']);

    return $client;
}
