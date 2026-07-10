import { redirect } from "next/navigation";
import { refreshAccessToken, toSession } from "@/lib/oidcClient";
import { getSession, isExpired, storeSession, type Session } from "@/lib/session";

const REFRESH_BUFFER_SECONDS = 10;

/**
 * Server Components can't set cookies themselves, so an expired access token is handled by
 * bouncing through the /refresh route handler (which can) and back to the current page.
 */
export async function requireSession(currentPath: string): Promise<Session> {
    const session = await getSession();
    if (!session) {
        redirect("/");
    }
    if (isExpired(session.accessTokenExpiresAt, REFRESH_BUFFER_SECONDS)) {
        redirect(`/refresh?next=${encodeURIComponent(currentPath)}`);
    }
    return session;
}

/**
 * For Server Actions (unlike page rendering, these CAN set cookies) -- refreshes inline
 * instead of redirecting, so a form submission doesn't get lost mid-mutation.
 */
export async function requireFreshAccessToken(): Promise<string> {
    const session = await getSession();
    if (!session) {
        redirect("/");
    }
    if (!isExpired(session.accessTokenExpiresAt, REFRESH_BUFFER_SECONDS)) {
        return session.accessToken;
    }

    const tokens = await refreshAccessToken(session.refreshToken);
    const refreshed = toSession(tokens, session.refreshToken);
    await storeSession(refreshed);
    return refreshed.accessToken;
}
