"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { updateSlaPolicy } from "@/lib/api";
import { requireFreshAccessToken } from "@/lib/auth";
import type { Severity } from "@/lib/types";

export async function updateSlaPolicyAction(severity: Severity, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const slaMinutes = Number(formData.get("slaMinutes"));
    const nearBreachPercentage = Number(formData.get("nearBreachPercentage"));

    await updateSlaPolicy(accessToken, severity, slaMinutes, nearBreachPercentage);

    revalidatePath("/sla-policies");
    redirect("/sla-policies");
}
