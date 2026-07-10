import { fetchDashboardAnalytics, fetchDashboardSummary } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { TrendChart } from "@/components/TrendChart";

export default async function DashboardPage() {
    const session = await requireSession("/dashboard");
    const [summary, analytics] = await Promise.all([
        fetchDashboardSummary(session.accessToken),
        fetchDashboardAnalytics(session.accessToken),
    ]);

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
                <h1 className="mb-6 text-2xl font-semibold">Dashboard</h1>
                <div className="mb-6 grid grid-cols-1 gap-6 sm:grid-cols-3">
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

                <div className="mb-6 grid grid-cols-1 gap-6 sm:grid-cols-2">
                    <SummaryCard
                        label="Průměrná doba řešení"
                        value={formatMinutes(analytics.avgResolutionMinutes)}
                        accent="border-violet-500/30 text-violet-300"
                    />
                    <SummaryCard
                        label="SLA compliance"
                        value={
                            analytics.slaComplianceRate === null
                                ? "—"
                                : `${analytics.slaComplianceRate.toFixed(0)} %`
                        }
                        accent="border-emerald-500/30 text-emerald-300"
                    />
                </div>

                <div className="rounded-2xl border border-white/10 bg-slate-900/60 p-6">
                    <p className="mb-4 text-xs font-semibold uppercase tracking-widest text-slate-500">
                        Vytvořené incidenty (posledních 14 dní)
                    </p>
                    {analytics.createdPerDay.length > 0 ? (
                        <TrendChart data={analytics.createdPerDay} />
                    ) : (
                        <p className="py-16 text-center text-sm text-slate-500">Zatím žádná data.</p>
                    )}
                </div>
            </main>
        </div>
    );
}

function SummaryCard({ label, value, accent }: { label: string; value: number | string; accent: string }) {
    return (
        <div className={`rounded-2xl border bg-slate-900/60 p-8 ${accent}`}>
            <p className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-500">{label}</p>
            <p className="text-4xl font-semibold">{value}</p>
        </div>
    );
}

function formatMinutes(minutes: number | null): string {
    if (minutes === null) return "—";
    const totalMinutes = Math.round(minutes);
    const hours = Math.floor(totalMinutes / 60);
    const mins = totalMinutes % 60;
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;
}
