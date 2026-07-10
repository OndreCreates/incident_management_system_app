"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { addTeamMember, createTeam } from "@/lib/api";
import { requireFreshAccessToken } from "@/lib/auth";

export async function createTeamAction(formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const name = String(formData.get("name") ?? "").trim();
    const memberEmails = String(formData.get("memberEmails") ?? "")
        .split(",")
        .map((email) => email.trim())
        .filter(Boolean);

    if (!name || memberEmails.length === 0) {
        redirect(`/teams?error=${encodeURIComponent("Název a alespoň jeden email člena jsou povinné.")}`);
    }

    await createTeam(accessToken, { name, memberEmails });

    revalidatePath("/teams");
    redirect("/teams");
}

export async function addTeamMemberAction(teamId: number, formData: FormData): Promise<void> {
    const accessToken = await requireFreshAccessToken();

    const userEmail = String(formData.get("userEmail") ?? "").trim();
    if (userEmail) {
        await addTeamMember(accessToken, teamId, userEmail);
    }

    revalidatePath(`/teams/${teamId}`);
    redirect(`/teams/${teamId}`);
}
