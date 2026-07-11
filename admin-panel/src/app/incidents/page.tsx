import Link from "next/link";
import { fetchIncidents } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import type { Severity, Status } from "@/lib/types";
import { Nav } from "@/components/Nav";
import { SeverityBadge, StatusBadge, BreachedBadge } from "@/components/Badges";
import { bulkAssignAction, bulkTransitionAction } from "./actions";

const STATUSES: Status[] = ["CREATED", "ASSIGNED", "INVESTIGATING", "MITIGATED", "RESOLVED", "CLOSED"];
const SEVERITIES: Severity[] = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];
const PAGE_SIZE = 20;

interface IncidentsPageProps {
    searchParams: Promise<{
        status?: string;
        severity?: string;
        assignedUserId?: string;
        q?: string;
        page?: string;
        error?: string;
        info?: string;
    }>;
}

export default async function IncidentsPage({ searchParams }: IncidentsPageProps) {
    const session = await requireSession("/incidents");
    const params = await searchParams;

    const status = (params.status as Status | undefined) || undefined;
    const severity = (params.severity as Severity | undefined) || undefined;
    const assignedUserId = params.assignedUserId || undefined;
    const q = params.q || undefined;
    const page = Number(params.page ?? 0) || 0;

    const incidentPage = await fetchIncidents(session.accessToken, {
        status,
        severity,
        assignedUserId,
        q,
        page,
        size: PAGE_SIZE,
    });

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
                <h1 className="mb-6 text-2xl font-semibold">Incidenty</h1>

                {params.error && (
                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                        {params.error}
                    </div>
                )}
                {params.info && (
                    <div className="mb-6 rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-300">
                        {params.info}
                    </div>
                )}

                <form method="get" className="mb-6 flex flex-wrap items-end gap-4">
                    <Field label="Hledat">
                        <input
                            type="text"
                            name="q"
                            defaultValue={q ?? ""}
                            placeholder="titulek nebo popis…"
                            className={selectClass}
                        />
                    </Field>
                    <Field label="Status">
                        <select name="status" defaultValue={status ?? ""} className={selectClass}>
                            <option value="">Všechny</option>
                            {STATUSES.map((s) => (
                                <option key={s} value={s}>
                                    {s}
                                </option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Severity">
                        <select name="severity" defaultValue={severity ?? ""} className={selectClass}>
                            <option value="">Všechny</option>
                            {SEVERITIES.map((s) => (
                                <option key={s} value={s}>
                                    {s}
                                </option>
                            ))}
                        </select>
                    </Field>
                    <Field label="Přiřazeno (email)">
                        <input
                            type="text"
                            name="assignedUserId"
                            defaultValue={assignedUserId ?? ""}
                            placeholder="uzivatel@example.com"
                            className={selectClass}
                        />
                    </Field>
                    <button
                        type="submit"
                        className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 hover:bg-slate-700"
                    >
                        Filtrovat
                    </button>
                    <a
                        href={exportHref({ status, severity, assignedUserId, q })}
                        className="rounded-lg border border-white/10 px-4 py-2 text-sm font-medium text-slate-300 hover:bg-white/5"
                    >
                        Export CSV
                    </a>
                </form>

                <form action={bulkTransitionAction}>
                    <div className="mb-3 flex flex-wrap items-end gap-3 rounded-xl border border-white/10 bg-slate-900/40 px-4 py-3">
                        <span className="text-xs uppercase tracking-wide text-slate-500">Hromadná akce pro vybrané:</span>
                        <Field label="Přejít do stavu">
                            <select name="bulkTargetStatus" defaultValue="ASSIGNED" className={selectClass}>
                                {STATUSES.map((s) => (
                                    <option key={s} value={s}>
                                        {s}
                                    </option>
                                ))}
                            </select>
                        </Field>
                        <button
                            type="submit"
                            formAction={bulkTransitionAction}
                            className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 hover:bg-slate-700"
                        >
                            Přejít do stavu
                        </button>
                        <Field label="Přiřadit uživateli (e-mail)">
                            <input
                                type="text"
                                name="bulkAssignedUserId"
                                placeholder="uzivatel@example.com"
                                className={selectClass}
                            />
                        </Field>
                        <button
                            type="submit"
                            formAction={bulkAssignAction}
                            className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium text-slate-100 hover:bg-slate-700"
                        >
                            Hromadně přiřadit
                        </button>
                    </div>

                    <div className="overflow-hidden rounded-xl border border-white/10">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-slate-900/60 text-xs uppercase tracking-wide text-slate-500">
                                <tr>
                                    <th className="px-4 py-3"></th>
                                    <th className="px-4 py-3">Titulek</th>
                                    <th className="px-4 py-3">Status</th>
                                    <th className="px-4 py-3">Severity</th>
                                    <th className="px-4 py-3">Priorita</th>
                                    <th className="px-4 py-3">Přiřazeno</th>
                                    <th className="px-4 py-3">SLA</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-white/5">
                                {incidentPage.content.map((incident) => (
                                    <tr key={incident.id} className="hover:bg-white/5">
                                        <td className="px-4 py-3">
                                            <input
                                                type="checkbox"
                                                name="incidentIds"
                                                value={incident.id}
                                                className="h-4 w-4 rounded border-white/20 bg-slate-900"
                                            />
                                        </td>
                                        <td className="px-4 py-3">
                                            <Link
                                                href={`/incidents/${incident.id}`}
                                                className="font-medium text-slate-100 hover:text-red-300"
                                            >
                                                {incident.title}
                                            </Link>
                                        </td>
                                        <td className="px-4 py-3">
                                            <StatusBadge status={incident.status} />
                                        </td>
                                        <td className="px-4 py-3">
                                            <SeverityBadge severity={incident.severity} />
                                        </td>
                                        <td className="px-4 py-3 text-slate-400">{incident.priority}</td>
                                        <td className="px-4 py-3 text-slate-400">{incident.assignedUserId ?? "—"}</td>
                                        <td className="px-4 py-3">{incident.slaBreached && <BreachedBadge />}</td>
                                    </tr>
                                ))}
                                {incidentPage.content.length === 0 && (
                                    <tr>
                                        <td colSpan={7} className="px-4 py-10 text-center text-slate-500">
                                            Žádné incidenty neodpovídají filtru.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </form>

                <Pagination
                    page={incidentPage.number}
                    totalPages={incidentPage.totalPages}
                    query={{ status, severity, assignedUserId, q }}
                />
            </main>
        </div>
    );
}

const selectClass =
    "rounded-lg border border-white/10 bg-slate-900 px-3 py-2 text-sm text-slate-100 focus:border-red-400 focus:outline-none";

function Field({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <label className="flex flex-col gap-1 text-xs text-slate-500">
            {label}
            {children}
        </label>
    );
}

function exportHref(query: Record<string, string | undefined>): string {
    const params = new URLSearchParams();
    Object.entries(query).forEach(([key, value]) => {
        if (value) params.set(key, value);
    });
    return `/incidents/export?${params.toString()}`;
}

function Pagination({
    page,
    totalPages,
    query,
}: {
    page: number;
    totalPages: number;
    query: Record<string, string | undefined>;
}) {
    if (totalPages <= 1) return null;

    function hrefFor(targetPage: number): string {
        const params = new URLSearchParams();
        Object.entries(query).forEach(([key, value]) => {
            if (value) params.set(key, value);
        });
        params.set("page", String(targetPage));
        return `/incidents?${params.toString()}`;
    }

    return (
        <div className="mt-6 flex items-center justify-between text-sm text-slate-400">
            <span>
                Stránka {page + 1} z {totalPages}
            </span>
            <div className="flex gap-2">
                {page > 0 && (
                    <Link href={hrefFor(page - 1)} className="rounded-lg border border-white/10 px-3 py-1.5 hover:bg-white/5">
                        Předchozí
                    </Link>
                )}
                {page + 1 < totalPages && (
                    <Link href={hrefFor(page + 1)} className="rounded-lg border border-white/10 px-3 py-1.5 hover:bg-white/5">
                        Další
                    </Link>
                )}
            </div>
        </div>
    );
}
