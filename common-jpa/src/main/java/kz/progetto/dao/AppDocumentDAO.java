package kz.progetto.dao;

import kz.progetto.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentDAO extends JpaRepository<AppDocument,Long> {
}
