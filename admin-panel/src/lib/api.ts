import { apiConfig } from "@/lib/config";
import type {
    Comment,
    DashboardAnalytics,
    DashboardSummary,
    Incident,
    IncidentDetail,
    IncidentPage,
    Postmortem,
    Severity,
    SlaPolicy,
    Status,
    Team,
} from "@/lib/types";

/** Thrown for any non-2xx backend response. Carries the parsed JSON body (when present) so
 * callers can branch on the shape GlobalExceptionHandler (backend) actually returns --
 * e.g. INVALID_TRANSITION's `allowed` list. */
export class ApiError extends Error {
    constructor(
        public readonly status: number,
        public readonly body: unknown,
    ) {
        super(`Incident API request failed with status ${status}`);
    }
}

async function apiFetch<T>(
    accessToken: string,
    path: string,
    init?: RequestInit,
): Promise<T> {
    const response = await fetch(new URL(path, apiConfig.baseUrl), {
        ...init,
        headers: {
            ...init?.headers,
            Authorization: `Bearer ${accessToken}`,
            ...(init?.body ? { "Content-Type": "application/json" } : {}),
        },
        cache: "no-store",
    });

    if (!response.ok) {
        let body: unknown = null;
        try {
            body = await response.json();
        } catch {
            // Non-JSON error body (e.g. a raw 401/403 from the security filter chain) -- ApiError.body stays null.
        }
        throw new ApiError(response.status, body);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return (await response.json()) as T;
}

export interface IncidentFilters {
    status?: Status;
    severity?: Severity;
    assignedUserId?: string;
    assignedTeamId?: number;
    q?: string;
    page?: number;
    size?: number;
}

export function fetchIncidents(accessToken: string, filters: IncidentFilters): Promise<IncidentPage> {
    const params = new URLSearchParams();
    if (filters.status) params.set("status", filters.status);
    if (filters.severity) params.set("severity", filters.severity);
    if (filters.assignedUserId) params.set("assignedUserId", filters.assignedUserId);
    if (filters.assignedTeamId) params.set("assignedTeamId", String(filters.assignedTeamId));
    if (filters.q) params.set("q", filters.q);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 20));

    return apiFetch<IncidentPage>(accessToken, `/api/v1/incidents?${params.toString()}`);
}

export function fetchIncidentDetail(accessToken: string, id: number): Promise<IncidentDetail> {
    return apiFetch<IncidentDetail>(accessToken, `/api/v1/incidents/${id}`);
}

export function fetchDashboardSummary(accessToken: string): Promise<DashboardSummary> {
    return apiFetch<DashboardSummary>(accessToken, "/api/v1/dashboard/summary");
}

export function fetchDashboardAnalytics(accessToken: string): Promise<DashboardAnalytics> {
    return apiFetch<DashboardAnalytics>(accessToken, "/api/v1/dashboard/analytics");
}

export function fetchSlaPolicies(accessToken: string): Promise<SlaPolicy[]> {
    return apiFetch<SlaPolicy[]>(accessToken, "/api/v1/sla-policies");
}

export function updateSlaPolicy(accessToken: string, severity: Severity, slaMinutes: number,
                                 nearBreachPercentage: number): Promise<SlaPolicy> {
    return apiFetch<SlaPolicy>(accessToken, `/api/v1/sla-policies/${severity}`, {
        method: "PUT",
        body: JSON.stringify({ slaMinutes, nearBreachPercentage }),
    });
}

export interface CreateIncidentInput {
    title: string;
    description?: string;
    severity: Severity;
    priority: string;
}

export function createIncident(accessToken: string, input: CreateIncidentInput): Promise<Incident> {
    return apiFetch<Incident>(accessToken, "/api/v1/incidents", {
        method: "POST",
        body: JSON.stringify(input),
    });
}

export interface TransitionInput {
    targetStatus: Status;
    note?: string;
    assignedUserId?: string;
}

export function transitionIncident(accessToken: string, id: number, input: TransitionInput): Promise<Incident> {
    return apiFetch<Incident>(accessToken, `/api/v1/incidents/${id}/transition`, {
        method: "POST",
        body: JSON.stringify(input),
    });
}

export function addComment(accessToken: string, id: number, content: string): Promise<Comment> {
    return apiFetch<Comment>(accessToken, `/api/v1/incidents/${id}/comments`, {
        method: "POST",
        body: JSON.stringify({ content }),
    });
}

export function assignTeam(accessToken: string, incidentId: number, teamId: number): Promise<Incident> {
    return apiFetch<Incident>(accessToken, `/api/v1/incidents/${incidentId}/assign-team`, {
        method: "POST",
        body: JSON.stringify({ teamId }),
    });
}

export function fetchTeams(accessToken: string): Promise<Team[]> {
    return apiFetch<Team[]>(accessToken, "/api/v1/teams");
}

export function fetchTeam(accessToken: string, id: number): Promise<Team> {
    return apiFetch<Team>(accessToken, `/api/v1/teams/${id}`);
}

export interface CreateTeamInput {
    name: string;
    memberEmails: string[];
}

export function createTeam(accessToken: string, input: CreateTeamInput): Promise<Team> {
    return apiFetch<Team>(accessToken, "/api/v1/teams", {
        method: "POST",
        body: JSON.stringify(input),
    });
}

export function addTeamMember(accessToken: string, teamId: number, userEmail: string): Promise<Team> {
    return apiFetch<Team>(accessToken, `/api/v1/teams/${teamId}/members`, {
        method: "POST",
        body: JSON.stringify({ userEmail }),
    });
}

export interface PostmortemInput {
    impact: string;
    rootCause: string;
    resolution: string;
    lessonsLearned: string;
    actionItems?: string;
}

export function fetchPostmortem(accessToken: string, incidentId: number): Promise<Postmortem> {
    return apiFetch<Postmortem>(accessToken, `/api/v1/incidents/${incidentId}/postmortem`);
}

export function createPostmortem(accessToken: string, incidentId: number,
                                  input: PostmortemInput): Promise<Postmortem> {
    return apiFetch<Postmortem>(accessToken, `/api/v1/incidents/${incidentId}/postmortem`, {
        method: "POST",
        body: JSON.stringify(input),
    });
}

export function updatePostmortem(accessToken: string, incidentId: number,
                                  input: PostmortemInput): Promise<Postmortem> {
    return apiFetch<Postmortem>(accessToken, `/api/v1/incidents/${incidentId}/postmortem`, {
        method: "PUT",
        body: JSON.stringify(input),
    });
}
