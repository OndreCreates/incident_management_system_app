import { fetchTeam } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { addTeamMemberAction } from "@/app/teams/actions";

interface TeamDetailPageProps {
    params: Promise<{ id: string }>;
}

export default async function TeamDetailPage({ params }: TeamDetailPageProps) {
    const { id } = await params;
    const session = await requireSession(`/teams/${id}`);
    const team = await fetchTeam(session.accessToken, Number(id));

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-2xl flex-1 px-6 py-10">
                <h1 className="mb-6 text-2xl font-semibold">{team.name}</h1>

                <div className="mb-6 rounded-xl border border-white/10 bg-slate-900/60 p-6">
                    <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Členové</h2>
                    <ul className="space-y-1 text-sm text-slate-300">
                        {team.memberEmails.map((email) => (
                            <li key={email}>{email}</li>
                        ))}
                    </ul>
                </div>

                <div className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                    <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">
                        Přidat člena
                    </h2>
                    <form action={addTeamMemberAction.bind(null, team.id)} className="flex gap-3">
                        <input
                            type="text"
                            name="userEmail"
                            required
                            placeholder="novy@example.com"
                            className="flex-1 rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                        />
                        <button
                            type="submit"
                            className="rounded-lg bg-slate-800 px-4 py-2 text-sm font-medium hover:bg-slate-700"
                        >
                            Přidat
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
}
