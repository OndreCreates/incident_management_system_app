import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { SubmitButton } from "@/components/SubmitButton";
import { createIncidentAction } from "@/app/incidents/actions";

interface NewIncidentPageProps {
    searchParams: Promise<{ error?: string }>;
}

export default async function NewIncidentPage({ searchParams }: NewIncidentPageProps) {
    await requireSession("/incidents/new");
    const { error } = await searchParams;

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-2xl flex-1 px-6 py-10">
                <h1 className="mb-6 text-2xl font-semibold">Nový incident</h1>

                {error && (
                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                        {error}
                    </div>
                )}

                <form action={createIncidentAction} className="space-y-5 rounded-xl border border-white/10 bg-slate-900/60 p-6">
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="title" className="text-sm text-slate-400">
                            Titulek
                        </label>
                        <input
                            id="title"
                            name="title"
                            required
                            maxLength={255}
                            className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                        />
                    </div>

                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="description" className="text-sm text-slate-400">
                            Popis
                        </label>
                        <textarea
                            id="description"
                            name="description"
                            rows={4}
                            className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="flex flex-col gap-1.5">
                            <label htmlFor="severity" className="text-sm text-slate-400">
                                Severity
                            </label>
                            <select
                                id="severity"
                                name="severity"
                                required
                                defaultValue="MEDIUM"
                                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                            >
                                <option value="CRITICAL">CRITICAL</option>
                                <option value="HIGH">HIGH</option>
                                <option value="MEDIUM">MEDIUM</option>
                                <option value="LOW">LOW</option>
                            </select>
                        </div>

                        <div className="flex flex-col gap-1.5">
                            <label htmlFor="priority" className="text-sm text-slate-400">
                                Priorita
                            </label>
                            <select
                                id="priority"
                                name="priority"
                                required
                                defaultValue="P2"
                                className="rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm focus:border-red-400 focus:outline-none"
                            >
                                <option value="P1">P1</option>
                                <option value="P2">P2</option>
                                <option value="P3">P3</option>
                                <option value="P4">P4</option>
                            </select>
                        </div>
                    </div>

                    <SubmitButton
                        className="w-full rounded-lg bg-red-500 px-4 py-2.5 text-sm font-semibold text-white hover:bg-red-400"
                        pendingLabel="Vytvářím…"
                    >
                        Vytvořit incident
                    </SubmitButton>
                </form>
            </main>
        </div>
    );
}
