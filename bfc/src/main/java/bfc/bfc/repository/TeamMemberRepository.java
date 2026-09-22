package bfc.bfc.repository;

import bfc.bfc.entities.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT COALESCE(MAX(t.displayOrder), 0) FROM TeamMember t")
    int findMaxDisplayOrder();
}
