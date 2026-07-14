import { notFound } from "next/navigation";
import { ApiError, fetchIncidentDetail, fetchPostmortem, fetchTeam, fetchTeams } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { verifyIdToken } from "@/lib/verifyIdToken";
import { ALLOWED_TRANSITIONS, TERMINAL_STATUSES, type Postmortem, type Status, type Team,
    type TimelineEntry } from "@/lib/types";
import { Nav } from "@/components/Nav";
import { SeverityBadge, StatusBadge, BreachedBadge } from "@/components/Badges";
import { SubmitButton } from "@/components/SubmitButton";
import {
    addCommentAction,
    assignTeamAction,
    createPostmortemAction,
    deleteCommentAction,
    editCommentAction,
    transitionIncidentAction,
    updatePostmortemAction,
} from "@/app/incidents/actions";

interface IncidentDetailPageProps {
    params: Promise<{ id: string }>;
    searchParams: Promise<{ error?: string }>;
}

export default async function IncidentDetailPage({ params, searchParams }: IncidentDetailPageProps) {
    const { id: idParam } = await params;
    const { error } = await searchParams;
    const id = Number(idParam);

    const session = await requireSession(`/incidents/${idParam}`);

    let detail;
    try {
        detail = await fetchIncidentDetail(session.accessToken, id);
    } catch (cause) {
        if (cause instanceof ApiError && cause.status === 404) {
            notFound();
        }
        throw cause;
    }

    const { incident, timeline } = detail;
    const nextStatuses = ALLOWED_TRANSITIONS[incident.status] ?? [];
    const isTerminal = TERMINAL_STATUSES.includes(incident.status);
    const currentUserEmail = (await verifyIdToken(session.idToken)).sub as string;

    const [teams, assignedTeam, postmortem] = await Promise.all([
        fetchTeams(session.accessToken),
        incident.assignedTeamId ? fetchTeam(session.accessToken, incident.assignedTeamId) : Promise.resolve(null),
        isTerminal ? fetchPostmortemOrNull(session.accessToken, id) : Promise.resolve(null),
    ]);

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto grid w-full max-w-6xl flex-1 gap-8 px-6 py-10 lg:grid-cols-3">
                <section className="lg:col-span-2 space-y-6">
                    {error && (
                        <div className="rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                            {error}
                        </div>
                    )}

                    <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                        <div className="mb-3 flex flex-wrap items-center gap-2">
                            <StatusBadge status={incident.status} />
                            <SeverityBadge severity={incident.severity} />
                            <span className="text-xs text-slate-500">{incident.priority}</span>
                            {incident.slaBreached && <BreachedBadge />}
                        </div>
                        <h1 className="mb-2 text-2xl font-semibold">{incident.title}</h1>
                        {incident.description && (
                            <p className="mb-4 text-sm leading-relaxed text-slate-400">{incident.description}</p>
                        )}
                        <dl className="grid grid-cols-2 gap-3 text-sm">
                            <Row label="Vytvořil" value={incident.createdBy} />
                            <Row label="Přiřazeno" value={incident.assignedUserId ?? "—"} />
                            <Row label="Tým" value={assignedTeam?.name ?? "—"} />
                            <Row label="SLA deadline" value={formatDate(incident.slaDeadline)} />
                            <Row label="Vytvořeno" value={formatDate(incident.createdAt)} />
                            {incident.rootCause && <Row label="Root cause" value={incident.rootCause} />}
                            {incident.resolution && <Row label="Resolution" value={incident.resolution} />}
                        </dl>
                    </div>

                    {nextStatuses.length > 0 && (
                        <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
                                Přechod stavu
                            </h2>
                            <div className="flex flex-wrap gap-4">
                                {nextStatuses.map((status) => (
                                    <TransitionForm key={status} incidentId={incident.id} status={status} />
                                ))}
                            </div>
                        </div>
                    )}

                    {teams.length > 0 && (
                        <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
                                Routovat na tým
                            </h2>
                            <form action={assignTeamAction.bind(null, incident.id)} className="flex gap-3">
                                <select
                                    name="teamId"
                                    defaultValue={incident.assignedTeamId ?? ""}
                                    className="flex-1 rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                                >
                                    <option value="" disabled>
                                        Vyber tým
                                    </option>
                                    {teams.map((team) => (
                                        <option key={team.id} value={team.id}>
                                            {team.name}
                                        </option>
                                    ))}
                                </select>
                                <SubmitButton className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium hover:bg-slate-700">
                                    Přiřadit
                                </SubmitButton>
                            </form>
                        </div>
                    )}

                    <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
                            Přidat komentář
                        </h2>
                        <form action={addCommentAction.bind(null, incident.id)} className="flex flex-col gap-3">
                            <textarea
                                name="content"
                                required
                                rows={3}
                                placeholder="Co jsi zjistil/a?"
                                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                            />
                            <SubmitButton className="self-start rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium hover:bg-slate-700">
                                Přidat komentář
                            </SubmitButton>
                        </form>
                    </div>

                    {isTerminal && (
                        <PostmortemSection incidentId={incident.id} postmortem={postmortem} />
                    )}
                </section>

                <section className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                    <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">Timeline</h2>
                    <ol className="space-y-4">
                        {timeline.map((entry) => (
                            <TimelineItem
                                key={entry.id}
                                entry={entry}
                                incidentId={incident.id}
                                currentUserEmail={currentUserEmail}
                            />
                        ))}
                        {timeline.length === 0 && <p className="text-sm text-slate-500">Zatím žádná aktivita.</p>}
                    </ol>
                </section>
            </main>
        </div>
    );
}

async function fetchPostmortemOrNull(accessToken: string, incidentId: number): Promise<Postmortem | null> {
    try {
        return await fetchPostmortem(accessToken, incidentId);
    } catch (cause) {
        if (cause instanceof ApiError && cause.status === 404) {
            return null;
        }
        throw cause;
    }
}

function PostmortemSection({ incidentId, postmortem }: { incidentId: number; postmortem: Postmortem | null }) {
    const action = postmortem ? updatePostmortemAction.bind(null, incidentId) : createPostmortemAction.bind(null, incidentId);

    return (
        <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
            <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
                Postmortem {postmortem ? "" : "(nový)"}
            </h2>
            <form action={action} className="flex flex-col gap-4">
                <PostmortemField name="impact" label="Dopad" defaultValue={postmortem?.impact} />
                <PostmortemField name="rootCause" label="Root cause" defaultValue={postmortem?.rootCause} />
                <PostmortemField name="resolution" label="Řešení" defaultValue={postmortem?.resolution} />
                <PostmortemField name="lessonsLearned" label="Lessons learned" defaultValue={postmortem?.lessonsLearned} />
                <PostmortemField
                    name="actionItems"
                    label="Action items (volitelné)"
                    defaultValue={postmortem?.actionItems ?? undefined}
                    required={false}
                />
                <SubmitButton
                    className="self-start rounded-lg bg-red-500 px-4 py-2 text-sm font-semibold text-white hover:bg-red-400"
                    pendingLabel="Ukládám…"
                >
                    {postmortem ? "Uložit změny" : "Vytvořit postmortem"}
                </SubmitButton>
            </form>
        </div>
    );
}

function PostmortemField({
    name,
    label,
    defaultValue,
    required = true,
}: {
    name: string;
    label: string;
    defaultValue?: string;
    required?: boolean;
}) {
    return (
        <div className="flex flex-col gap-1.5">
            <label htmlFor={name} className="text-xs text-slate-500">
                {label}
            </label>
            <textarea
                id={name}
                name={name}
                required={required}
                defaultValue={defaultValue}
                rows={3}
                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
            />
        </div>
    );
}

function TransitionForm({ incidentId, status }: { incidentId: number; status: Status }) {
    return (
        <form
            action={transitionIncidentAction.bind(null, incidentId)}
            className="flex flex-col gap-2 rounded-lg border border-white/10 bg-slate-950/50 p-4"
        >
            <input type="hidden" name="targetStatus" value={status} />
            {status === "ASSIGNED" && (
                <input
                    type="text"
                    name="assignedUserId"
                    placeholder="Přiřadit uživateli (email)"
                    className="rounded-lg border border-white/10 bg-slate-950 px-2.5 py-1.5 text-xs focus:border-red-400 focus:outline-none"
                />
            )}
            <input
                type="text"
                name="note"
                placeholder="Poznámka (volitelné)"
                className="rounded-lg border border-white/10 bg-slate-950 px-2.5 py-1.5 text-xs focus:border-red-400 focus:outline-none"
            />
            <SubmitButton className="rounded-lg bg-red-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-400">
                → {status}
            </SubmitButton>
        </form>
    );
}

function TimelineItem({
    entry,
    incidentId,
    currentUserEmail,
}: {
    entry: TimelineEntry;
    incidentId: number;
    currentUserEmail: string;
}) {
    const label = (() => {
        switch (entry.eventType) {
            case "STATUS_CHANGE":
                return `${entry.fromStatus} → ${entry.toStatus}`;
            case "ASSIGNMENT":
                return "Přiřazení";
            case "TEAM_ASSIGNMENT":
                return "Routováno na tým";
            case "COMMENT":
                return "Komentář";
        }
    })();

    const isOwnComment = entry.eventType === "COMMENT" && entry.commentId !== null
        && entry.actorUserId === currentUserEmail && !entry.commentDeleted;

    return (
        <li className="border-l-2 border-red-500/40 pl-4">
            <p className="text-xs text-slate-500">{formatDate(entry.createdAt)}</p>
            <p className="text-sm font-medium text-slate-200">{label}</p>
            <p className="text-xs text-slate-400">{entry.actorUserId}</p>
            {entry.note && <p className="mt-1 text-sm text-slate-400">{entry.note}</p>}
            {entry.commentDeleted && <p className="mt-1 text-sm italic text-slate-600">[komentář smazán]</p>}
            {!entry.commentDeleted && entry.commentContent && (
                <p className="mt-1 text-sm text-slate-300">
                    {entry.commentContent}
                    {entry.commentEdited && <span className="ml-2 text-xs text-slate-600">(upraveno)</span>}
                </p>
            )}
            {isOwnComment && (
                <div className="mt-2 flex items-center gap-4">
                    <details className="text-xs">
                        <summary className="cursor-pointer text-slate-500 hover:text-slate-300">Upravit</summary>
                        <form
                            action={editCommentAction.bind(null, incidentId, entry.commentId as number)}
                            className="mt-2 flex flex-col gap-2"
                        >
                            <textarea
                                name="content"
                                defaultValue={entry.commentContent ?? ""}
                                rows={2}
                                className="rounded-lg border border-white/10 bg-slate-950 px-2.5 py-1.5 text-xs focus:border-red-400 focus:outline-none"
                            />
                            <SubmitButton className="self-start rounded-lg bg-slate-800 px-3 py-1 text-xs font-medium hover:bg-slate-700">
                                Uložit
                            </SubmitButton>
                        </form>
                    </details>
                    <form action={deleteCommentAction.bind(null, incidentId, entry.commentId as number)}>
                        <SubmitButton className="text-xs text-red-400 hover:text-red-300" pendingLabel="Mažu…">
                            Smazat
                        </SubmitButton>
                    </form>
                </div>
            )}
        </li>
    );
}

function Row({ label, value }: { label: string; value: string }) {
    return (
        <div>
            <dt className="text-xs text-slate-500">{label}</dt>
            <dd className="text-slate-200">{value}</dd>
        </div>
    );
}

function formatDate(iso: string): string {
    return new Date(iso).toLocaleString("cs-CZ");
}
