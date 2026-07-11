import { NextRequest, NextResponse } from "next/server";
import { apiConfig } from "@/lib/config";
import { requireFreshAccessToken } from "@/lib/auth";

/**
 * A plain <a href> can't attach an Authorization header, so the CSV download
 * goes through this route handler: it holds the access token server-side and
 * proxies the backend's response straight through to the browser.
 */
export async function GET(request: NextRequest): Promise<NextResponse> {
    const accessToken = await requireFreshAccessToken();

    const backendUrl = new URL("/api/v1/incidents/export", apiConfig.baseUrl);
    backendUrl.search = request.nextUrl.search;

    const response = await fetch(backendUrl, {
        headers: { Authorization: `Bearer ${accessToken}` },
        cache: "no-store",
    });

    return new NextResponse(response.body, {
        status: response.status,
        headers: {
            "Content-Type": response.headers.get("Content-Type") ?? "text/csv",
            "Content-Disposition": response.headers.get("Content-Disposition") ?? "attachment; filename=\"incidents.csv\"",
        },
    });
}
