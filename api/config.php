<?php

declare(strict_types=1);

function database(): PDO
{
    static $db = null;

    if ($db instanceof PDO) {
        return $db;
    }

    $databaseUrl = trim((string) getenv('DATABASE_URL'));

    if ($databaseUrl !== '') {
        $parts = parse_url($databaseUrl);

        if ($parts === false || empty($parts['host'])) {
            throw new RuntimeException('DATABASE_URL is invalid.');
        }

        $host = $parts['host'];
        $port = (string)($parts['port'] ?? 5432);
        $name = ltrim((string)($parts['path'] ?? '/postgres'), '/');
        $user = isset($parts['user'])
            ? rawurldecode($parts['user'])
            : 'postgres';
        $password = isset($parts['pass'])
            ? rawurldecode($parts['pass'])
            : '';

        $dsn = sprintf(
            'pgsql:host=%s;port=%s;dbname=%s;sslmode=require',
            $host,
            $port,
            $name
        );
    } else {
        $host = getenv('DB_HOST') ?: 'postgres';
        $port = getenv('DB_PORT') ?: '5432';
        $name = getenv('DB_DATABASE') ?: 'payments';
        $user = getenv('DB_USERNAME') ?: 'payments';
        $password = getenv('DB_PASSWORD') ?: 'payments_dev_password';

        $dsn = sprintf(
            'pgsql:host=%s;port=%s;dbname=%s',
            $host,
            $port,
            $name
        );
    }

    $db = new PDO(
        $dsn,
        $user,
        $password,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false
        ]
    );

    return $db;
}
