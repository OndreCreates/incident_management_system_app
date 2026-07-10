package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // LEFT JOIN FETCH members: open-in-view is false, so team.getMembers() outside the
    // originating transaction would otherwise throw LazyInitializationException -- same
    // reasoning as IncidentTimelineRepository's comment fetch.
    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members ORDER BY t.name ASC")
    List<Team> findAllWithMembers();

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<Team> findByIdWithMembers(@Param("id") Long id);
}
