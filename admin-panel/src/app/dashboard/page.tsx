import { fetchDashboardSummary } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";

export default async function DashboardPage() {
    const session = await requireSession("/dashboard");
    const summary = await fetchDashboardSummary(session.accessToken);

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
                <h1 className="mb-6 text-2xl font-semibold">Dashboard</h1>
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
                    <SummaryCard label="Aktivní incidenty" value={summary.activeCount} accent="border-sky-500/30 text-sky-300" />
                    <SummaryCard
                        label="Kritické (aktivní)"
                        value={summary.criticalCount}
                        accent="border-orange-500/30 text-orange-300"
                    />
                    <SummaryCard
                        label="SLA porušeno"
                        value={summary.breachedCount}
                        accent="border-red-500/30 text-red-300"
                    />
                </div>
            </main>
        </div>
    );
}

function SummaryCard({ label, value, accent }: { label: string; value: number; accent: string }) {
    return (
        <div className={`rounded-2xl border bg-slate-900/60 p-8 ${accent}`}>
            <p className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-500">{label}</p>
            <p className="text-4xl font-semibold">{value}</p>
        </div>
    );
}
