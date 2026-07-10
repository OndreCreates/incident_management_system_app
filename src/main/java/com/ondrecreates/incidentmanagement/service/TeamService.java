package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Team;
import com.ondrecreates.incidentmanagement.exception.TeamNotFoundException;
import com.ondrecreates.incidentmanagement.repository.TeamRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Team createTeam(String name, List<String> memberEmails) {
        Team team = new Team(name);
        memberEmails.forEach(team::addMember);
        return teamRepository.save(team);
    }

    public List<Team> listTeams() {
        return teamRepository.findAllWithMembers();
    }

    public Team getTeamOrThrow(Long id) {
        return teamRepository.findByIdWithMembers(id).orElseThrow(() -> new TeamNotFoundException(id));
    }

    @Transactional
    public Team addMember(Long teamId, String userEmail) {
        Team team = getTeamOrThrow(teamId);
        team.addMember(userEmail);
        return teamRepository.save(team);
    }
}
