package bfc.bfc.repositories;

import bfc.bfc.entities.HistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryEventRepository extends JpaRepository<HistoryEvent, Long> {
    List<HistoryEvent> findAllByOrderByEventYearAsc();
}
