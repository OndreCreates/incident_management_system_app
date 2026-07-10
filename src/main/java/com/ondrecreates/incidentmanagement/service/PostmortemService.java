package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentPostmortem;
import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.PostmortemRequest;
import com.ondrecreates.incidentmanagement.exception.PostmortemAlreadyExistsException;
import com.ondrecreates.incidentmanagement.exception.PostmortemNotAllowedException;
import com.ondrecreates.incidentmanagement.exception.PostmortemNotFoundException;
import com.ondrecreates.incidentmanagement.repository.IncidentPostmortemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A postmortem only makes sense once the incident is actually done (Resolved
 * or Closed) -- writing one for an incident still mid-investigation would be
 * documenting a story that hasn't finished yet.
 */
@Service
public class PostmortemService {

    private final IncidentPostmortemRepository postmortemRepository;
    private final IncidentService incidentService;

    public PostmortemService(IncidentPostmortemRepository postmortemRepository, IncidentService incidentService) {
        this.postmortemRepository = postmortemRepository;
        this.incidentService = incidentService;
    }

    @Transactional
    public IncidentPostmortem create(Long incidentId, PostmortemRequest request, String authorUserId) {
        Incident incident = incidentService.getIncidentOrThrow(incidentId);
        requireTerminal(incident);
        if (postmortemRepository.findByIncidentId(incidentId).isPresent()) {
            throw new PostmortemAlreadyExistsException(incidentId);
        }
        IncidentPostmortem postmortem = new IncidentPostmortem(incidentId, request.impact(), request.rootCause(),
                request.resolution(), request.lessonsLearned(), request.actionItems(), authorUserId);
        return postmortemRepository.save(postmortem);
    }

    public IncidentPostmortem getOrThrow(Long incidentId) {
        return postmortemRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new PostmortemNotFoundException(incidentId));
    }

    @Transactional
    public IncidentPostmortem update(Long incidentId, PostmortemRequest request) {
        IncidentPostmortem postmortem = getOrThrow(incidentId);
        postmortem.update(request.impact(), request.rootCause(), request.resolution(), request.lessonsLearned(),
                request.actionItems());
        return postmortemRepository.save(postmortem);
    }

    private void requireTerminal(Incident incident) {
        if (!Status.TERMINAL_STATUSES.contains(incident.getStatus())) {
            throw new PostmortemNotAllowedException(incident.getId(), incident.getStatus());
        }
    }
}
