package bfc.bfc.repository;

import bfc.bfc.entities.ClientLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientLogoRepository extends JpaRepository<ClientLogo, Long> {
    List<ClientLogo> findAllByOrderByDisplayOrderAsc();
}
