<?php

declare(strict_types=1);

function createApiKeyRecord(
    PDO $db,
    string $projectId,
    string $environment
): array {
    $secret = generateProjectApiKey($environment);

    $stmt = $db->prepare(
        'INSERT INTO api_keys
        (project_id, key_prefix, key_hash, environment)
        VALUES
        (:project_id, :key_prefix, :key_hash, :environment)
        RETURNING id, key_prefix, environment, created_at'
    );

    $stmt->execute([
        ':project_id' => $projectId,
        ':key_prefix' => $secret['prefix'],
        ':key_hash' => $secret['hash'],
        ':environment' => $secret['environment']
    ]);

    $record = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$record) {
        throw new RuntimeException(
            'Unable to create API key.'
        );
    }

    return [
        'id' => $record['id'],
        'key' => $secret['secret'],
        'key_prefix' => $record['key_prefix'],
        'environment' => $record['environment'],
        'created_at' => $record['created_at']
    ];
}
