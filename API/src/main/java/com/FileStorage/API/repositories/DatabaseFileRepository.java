package com.FileStorage.API.repositories;

import com.FileStorage.API.models.DatabaseFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseFileRepository extends JpaRepository<DatabaseFile, Long> {
}
