package kz.progetto.dao;

import kz.progetto.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataDAO extends JpaRepository<RawData,Long> {
}
