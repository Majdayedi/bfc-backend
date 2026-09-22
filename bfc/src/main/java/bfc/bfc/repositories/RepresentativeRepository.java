package bfc.bfc.repositories;

import bfc.bfc.entities.Representative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, Long> {
    Optional<Representative> findBySlug(String slug);
}
