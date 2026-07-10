export type Severity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";
export type Priority = "P1" | "P2" | "P3" | "P4";
export type Status = "CREATED" | "ASSIGNED" | "INVESTIGATING" | "MITIGATED" | "RESOLVED" | "CLOSED";
export type EventType = "STATUS_CHANGE" | "ASSIGNMENT" | "TEAM_ASSIGNMENT" | "COMMENT";

export const TERMINAL_STATUSES: Status[] = ["RESOLVED", "CLOSED"];

export interface Incident {
    id: number;
    title: string;
    description: string | null;
    severity: Severity;
    priority: Priority;
    status: Status;
    assignedUserId: string | null;
    assignedTeamId: number | null;
    slaDeadline: string;
    slaBreached: boolean;
    rootCause: string | null;
    resolution: string | null;
    createdBy: string;
    createdAt: string;
    updatedAt: string;
}

export interface TimelineEntry {
    id: number;
    eventType: EventType;
    fromStatus: Status | null;
    toStatus: Status | null;
    commentId: number | null;
    commentContent: string | null;
    actorUserId: string;
    note: string | null;
    createdAt: string;
}

export interface IncidentDetail {
    incident: Incident;
    timeline: TimelineEntry[];
}

export interface Comment {
    id: number;
    authorUserId: string;
    content: string;
    createdAt: string;
}

export interface DashboardSummary {
    activeCount: number;
    criticalCount: number;
    breachedCount: number;
}

export interface IncidentPage {
    content: Incident[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export interface InvalidTransitionBody {
    error: "INVALID_TRANSITION";
    from: Status;
    attempted: Status;
    allowed: Status[];
}

export interface Team {
    id: number;
    name: string;
    memberEmails: string[];
    createdAt: string;
}

export interface Postmortem {
    id: number;
    incidentId: number;
    impact: string;
    rootCause: string;
    resolution: string;
    lessonsLearned: string;
    actionItems: string | null;
    authorUserId: string;
    createdAt: string;
    updatedAt: string;
}

/** Backend's ALLOWED_TRANSITIONS map, mirrored so the UI only ever renders buttons for
 * transitions the API will actually accept -- see IncidentTransitionService (backend) for
 * the source of truth this must stay in sync with. */
export const ALLOWED_TRANSITIONS: Record<Status, Status[]> = {
    CREATED: ["ASSIGNED"],
    ASSIGNED: ["INVESTIGATING"],
    INVESTIGATING: ["MITIGATED", "ASSIGNED"],
    MITIGATED: ["RESOLVED", "INVESTIGATING"],
    RESOLVED: ["CLOSED", "INVESTIGATING"],
    CLOSED: ["INVESTIGATING"],
};
