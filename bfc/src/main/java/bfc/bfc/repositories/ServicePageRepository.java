package bfc.bfc.repositories;

import bfc.bfc.entities.ServicePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePageRepository extends JpaRepository<ServicePage, Long> {
    Optional<ServicePage> findBySlug(String slug);
    List<ServicePage> findAllByOrderByDisplayOrderAsc();
}
