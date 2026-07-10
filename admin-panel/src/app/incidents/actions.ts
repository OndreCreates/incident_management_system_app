"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { ApiError, addComment, createIncident, transitionIncident } from "@/lib/api";
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

export async function addCommentAction(id: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const content = String(formData.get("content") ?? "").trim();
    if (content) {
        await addComment(accessToken, id, content);
    }

    revalidatePath(`/incidents/${id}`);
    redirect(`/incidents/${id}`);
}
