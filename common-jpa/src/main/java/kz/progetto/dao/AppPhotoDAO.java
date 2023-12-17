package kz.progetto.dao;

import kz.progetto.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoDAO extends JpaRepository<AppPhoto,Long> {
}
