function requireEnv(name: string): string {
    const value = process.env[name];
    if (!value) {
        throw new Error(`Missing required environment variable: ${name}`);
    }
    return value;
}

export const oidcConfig = {
    // The browser-facing address: baked into every token as `iss`, and where the browser
    // itself gets redirected for /oauth2/authorize. Must be reachable from the user's machine.
    issuer: requireEnv("IDENTITY_SERVER_ISSUER"),
    // Where THIS SERVER reaches the identity server directly (token exchange, revoke, JWKS).
    // Only differs from `issuer` when the two run as separate Docker Compose services --
    // defaults to `issuer` so local (non-Docker) setups don't need to set it at all.
    internalUrl: process.env.IDENTITY_SERVER_INTERNAL_URL ?? requireEnv("IDENTITY_SERVER_ISSUER"),
    clientId: requireEnv("OAUTH_CLIENT_ID"),
    // Confidential client -- only ever read server-side (route handlers/server actions), never sent to the browser.
    clientSecret: requireEnv("OAUTH_CLIENT_SECRET"),
    redirectUri: requireEnv("OAUTH_REDIRECT_URI"),
} as const;

// This app's own public address, derived from redirectUri rather than the incoming
// request's Host header -- see demo-client's config.ts (identity_server_app) for the full
// rationale: Next's standalone server builds absolute URLs from process.env.HOSTNAME, which
// in Docker is the container's own auto-assigned hostname, not something the browser can reach.
export const publicOrigin = new URL(oidcConfig.redirectUri).origin;

export const apiConfig = {
    // Where THIS SERVER reaches the incident_management_app backend (server components /
    // server actions only -- the access token never leaves the server, so the browser never
    // calls this API directly).
    baseUrl: requireEnv("INCIDENT_API_URL"),
} as const;
