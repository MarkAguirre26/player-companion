package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.GamesArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamesArchiveRepository extends JpaRepository<GamesArchive, Integer> {

    GamesArchive findByJournalId(Long journalId);

}
