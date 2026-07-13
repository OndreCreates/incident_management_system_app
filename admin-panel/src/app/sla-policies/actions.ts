"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { ApiError, updateSlaPolicy } from "@/lib/api";
import { requireFreshAccessToken } from "@/lib/auth";
import type { Severity } from "@/lib/types";

export async function updateSlaPolicyAction(severity: Severity, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const slaMinutes = Number(formData.get("slaMinutes"));
    const nearBreachPercentage = Number(formData.get("nearBreachPercentage"));

    try {
        await updateSlaPolicy(accessToken, severity, slaMinutes, nearBreachPercentage);
    } catch (cause) {
        // Belt-and-suspenders: the UI already hides this form for non-admins, but the
        // endpoint itself is still the actual authority (see AuthorizationService, backend).
        if (cause instanceof ApiError && cause.status === 403) {
            redirect(`/sla-policies?error=${encodeURIComponent("Jen ADMIN může měnit SLA politiky.")}`);
        }
        throw cause;
    }

    revalidatePath("/sla-policies");
    redirect("/sla-policies");
}
