<?php

declare(strict_types=1);

function createProject(
    PDO $db,
    string $clientId,
    string $name,
    string $description = ''
): array {
    $name = trim($name);
    $description = trim($description);

    if ($name === '') {
        throw new InvalidArgumentException(
            'Project name is required.'
        );
    }

    $stmt = $db->prepare(
        'INSERT INTO projects
         (client_id, name, description)
         VALUES
         (:client_id, :name, :description)
         RETURNING id, name, description, created_at'
    );

    $stmt->execute([
        ':client_id' => $clientId,
        ':name' => $name,
        ':description' => $description
    ]);

    $project = $stmt->fetch();

    if (!$project) {
        throw new RuntimeException(
            'Unable to create project.'
        );
    }

    return $project;
}

function listProjects(
    PDO $db,
    string $clientId
): array {
    $stmt = $db->prepare(
        'SELECT
            id,
            name,
            description,
            created_at
         FROM projects
         WHERE client_id = :client_id
         ORDER BY created_at DESC'
    );

    $stmt->execute([
        ':client_id' => $clientId
    ]);

    return $stmt->fetchAll();
}

function projectBelongsToClient(
    PDO $db,
    string $projectId,
    string $clientId
): bool {
    $stmt = $db->prepare(
        'SELECT 1
         FROM projects
         WHERE id = :project_id
         AND client_id = :client_id
         LIMIT 1'
    );

    $stmt->execute([
        ':project_id' => $projectId,
        ':client_id' => $clientId
    ]);

    return (bool)$stmt->fetchColumn();
}
