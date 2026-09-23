package bfc.bfc.repository;

import bfc.bfc.entities.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    List<Partner> findAllByOrderByDisplayOrderAsc();
}
