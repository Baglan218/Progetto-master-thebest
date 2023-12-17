package kz.progetto.dao;

import kz.progetto.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentDAO extends JpaRepository<BinaryContent,Long> {
}
