<?php

declare(strict_types=1);

function database(): PDO
{
    static $db = null;

    if ($db instanceof PDO) {
        return $db;
    }

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

    $db = new PDO(
        $dsn,
        $user,
        $password,
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]
    );

    return $db;
}
