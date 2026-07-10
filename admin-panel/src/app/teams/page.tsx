import Link from "next/link";
import { fetchTeams } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { createTeamAction } from "@/app/teams/actions";

interface TeamsPageProps {
    searchParams: Promise<{ error?: string }>;
}

export default async function TeamsPage({ searchParams }: TeamsPageProps) {
    const session = await requireSession("/teams");
    const { error } = await searchParams;
    const teams = await fetchTeams(session.accessToken);

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto grid w-full max-w-5xl flex-1 gap-8 px-6 py-10 lg:grid-cols-3">
                <section className="lg:col-span-2">
                    <h1 className="mb-6 text-2xl font-semibold">Týmy</h1>
                    <div className="overflow-hidden rounded-xl border border-white/10">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-slate-900/60 text-xs uppercase tracking-wide text-slate-500">
                                <tr>
                                    <th className="px-4 py-3">Název</th>
                                    <th className="px-4 py-3">Členové</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-white/5">
                                {teams.map((team) => (
                                    <tr key={team.id} className="hover:bg-white/5">
                                        <td className="px-4 py-3">
                                            <Link
                                                href={`/teams/${team.id}`}
                                                className="font-medium text-slate-100 hover:text-red-300"
                                            >
                                                {team.name}
                                            </Link>
                                        </td>
                                        <td className="px-4 py-3 text-slate-400">{team.memberEmails.join(", ")}</td>
                                    </tr>
                                ))}
                                {teams.length === 0 && (
                                    <tr>
                                        <td colSpan={2} className="px-4 py-10 text-center text-slate-500">
                                            Zatím žádné týmy.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>

                <section className="rounded-xl border border-white/10 bg-slate-900/60 p-6">
                    <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">Nový tým</h2>
                    {error && (
                        <div className="mb-4 rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-xs text-red-300">
                            {error}
                        </div>
                    )}
                    <form action={createTeamAction} className="flex flex-col gap-3">
                        <div className="flex flex-col gap-1.5">
                            <label htmlFor="name" className="text-xs text-slate-500">
                                Název
                            </label>
                            <input
                                id="name"
                                name="name"
                                required
                                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                            />
                        </div>
                        <div className="flex flex-col gap-1.5">
                            <label htmlFor="memberEmails" className="text-xs text-slate-500">
                                Emaily členů (oddělené čárkou)
                            </label>
                            <textarea
                                id="memberEmails"
                                name="memberEmails"
                                required
                                rows={3}
                                placeholder="a@example.com, b@example.com"
                                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                            />
                        </div>
                        <button
                            type="submit"
                            className="rounded-lg bg-red-500 px-4 py-2 text-sm font-semibold text-white hover:bg-red-400"
                        >
                            Vytvořit tým
                        </button>
                    </form>
                </section>
            </main>
        </div>
    );
}
