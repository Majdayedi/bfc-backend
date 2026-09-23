package bfc.bfc.repositories;

import bfc.bfc.entities.ContactServiceOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactServiceOptionRepository extends JpaRepository<ContactServiceOption, Long> {
    List<ContactServiceOption> findAllByOrderByDisplayOrderAsc();
}
