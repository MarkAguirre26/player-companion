package com.virtual.app.sicbo.module.repository;

import com.virtual.app.sicbo.module.data.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
}
