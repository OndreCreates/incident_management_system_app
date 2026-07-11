"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import {
    ApiError,
    addComment,
    assignTeam,
    bulkAssign,
    bulkTransition,
    createIncident,
    createPostmortem,
    deleteComment,
    editComment,
    transitionIncident,
    updatePostmortem,
} from "@/lib/api";
import { requireFreshAccessToken } from "@/lib/auth";
import type { Priority, Severity, Status } from "@/lib/types";

export async function createIncidentAction(formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const title = String(formData.get("title") ?? "").trim();
    const description = String(formData.get("description") ?? "").trim();
    const severity = String(formData.get("severity")) as Severity;
    const priority = String(formData.get("priority")) as Priority;

    if (!title || !severity || !priority) {
        redirect(`/incidents/new?error=${encodeURIComponent("Titulek, severity a priority jsou povinné.")}`);
    }

    const incident = await createIncident(accessToken, {
        title,
        description: description || undefined,
        severity,
        priority,
    });

    revalidatePath("/incidents");
    redirect(`/incidents/${incident.id}`);
}

export async function transitionIncidentAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const targetStatus = String(formData.get("targetStatus")) as Status;
    const noteRaw = String(formData.get("note") ?? "").trim();
    const assignedUserIdRaw = String(formData.get("assignedUserId") ?? "").trim();

    try {
        await transitionIncident(accessToken, id, {
            targetStatus,
            note: noteRaw || undefined,
            assignedUserId: assignedUserIdRaw || undefined,
        });
    } catch (cause) {
        if (cause instanceof ApiError && cause.status === 409) {
            const body = cause.body as { allowed?: Status[] } | null;
            const allowed = body?.allowed?.length ? body.allowed.join(", ") : "žádné";
            redirect(
                `/incidents/${id}?error=${encodeURIComponent(
                    `Neplatný přechod na ${targetStatus}. Aktuálně povolené: ${allowed}.`,
                )}`,
            );
        }
        throw cause;
    }

    revalidatePath(`/incidents/${id}`);
    revalidatePath("/incidents");
    redirect(`/incidents/${id}`);
}

export async function bulkTransitionAction(formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const incidentIds = formData.getAll("incidentIds").map(Number);
    const targetStatus = String(formData.get("bulkTargetStatus")) as Status;

    if (incidentIds.length === 0) {
        redirect(`/incidents?error=${encodeURIComponent("Vyber aspoň jeden incident.")}`);
    }

    const results = await bulkTransition(accessToken, incidentIds, targetStatus);
    revalidatePath("/incidents");
    redirect(`/incidents?info=${encodeURIComponent(summarize(results, `přesunuto do stavu ${targetStatus}`))}`);
}

export async function bulkAssignAction(formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const incidentIds = formData.getAll("incidentIds").map(Number);
    const assignedUserId = String(formData.get("bulkAssignedUserId") ?? "").trim();

    if (incidentIds.length === 0) {
        redirect(`/incidents?error=${encodeURIComponent("Vyber aspoň jeden incident.")}`);
    }
    if (!assignedUserId) {
        redirect(`/incidents?error=${encodeURIComponent("Zadej e-mail uživatele pro hromadné přiřazení.")}`);
    }

    const results = await bulkAssign(accessToken, incidentIds, assignedUserId);
    revalidatePath("/incidents");
    redirect(`/incidents?info=${encodeURIComponent(summarize(results, `přiřazeno uživateli ${assignedUserId}`))}`);
}

function summarize(results: { success: boolean }[], actionLabel: string): string {
    const failed = results.filter((r) => !r.success).length;
    const okCount = results.length - failed;
    return failed === 0
        ? `Úspěšně ${actionLabel}: ${okCount} incidentů.`
        : `${actionLabel}: ${okCount} úspěšně, ${failed} selhalo (neplatná operace pro daný incident).`;
}

export async function addCommentAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const content = String(formData.get("content") ?? "").trim();
    if (content) {
        await addComment(accessToken, id, content);
    }

    revalidatePath(`/incidents/${id}`);
    redirect(`/incidents/${id}`);
}

export async function editCommentAction(incidentId: number, commentId: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const content = String(formData.get("content") ?? "").trim();
    if (content) {
        await editComment(accessToken, incidentId, commentId, content);
    }

    revalidatePath(`/incidents/${incidentId}`);
    redirect(`/incidents/${incidentId}`);
}

export async function deleteCommentAction(incidentId: number, commentId: number): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    await deleteComment(accessToken, incidentId, commentId);

    revalidatePath(`/incidents/${incidentId}`);
    redirect(`/incidents/${incidentId}`);
}

export async function assignTeamAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const teamId = Number(formData.get("teamId"));
    if (teamId) {
        await assignTeam(accessToken, id, teamId);
    }

    revalidatePath(`/incidents/${id}`);
    redirect(`/incidents/${id}`);
}

function postmortemInputFrom(formData: FormData) {
    const actionItems = String(formData.get("actionItems") ?? "").trim();
    return {
        impact: String(formData.get("impact") ?? "").trim(),
        rootCause: String(formData.get("rootCause") ?? "").trim(),
        resolution: String(formData.get("resolution") ?? "").trim(),
        lessonsLearned: String(formData.get("lessonsLearned") ?? "").trim(),
        actionItems: actionItems || undefined,
    };
}

export async function createPostmortemAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    try {
        await createPostmortem(accessToken, id, postmortemInputFrom(formData));
    } catch (cause) {
        if (cause instanceof ApiError && cause.status === 409) {
            const body = cause.body as { message?: string } | null;
            redirect(`/incidents/${id}?error=${encodeURIComponent(body?.message ?? "Postmortem se nepodařilo vytvořit.")}`);
        }
        throw cause;
    }

    revalidatePath(`/incidents/${id}`);
    redirect(`/incidents/${id}`);
}

export async function updatePostmortemAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    await updatePostmortem(accessToken, id, postmortemInputFrom(formData));

    revalidatePath(`/incidents/${id}`);
    redirect(`/incidents/${id}`);
}
