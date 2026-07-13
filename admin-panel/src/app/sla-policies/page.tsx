import { fetchMe, fetchSlaPolicies } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { updateSlaPolicyAction } from "@/app/sla-policies/actions";

interface SlaPoliciesPageProps {
    searchParams: Promise<{ error?: string }>;
}

export default async function SlaPoliciesPage({ searchParams }: SlaPoliciesPageProps) {
    const session = await requireSession("/sla-policies");
    const { error } = await searchParams;
    const [policies, me] = await Promise.all([
        fetchSlaPolicies(session.accessToken),
        fetchMe(session.accessToken),
    ]);
    const isAdmin = me.role === "ADMIN";

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-b from-slate-950 to-slate-900 text-slate-100">
            <Nav />
            <main className="mx-auto w-full max-w-3xl flex-1 px-6 py-10">
                <h1 className="mb-2 text-2xl font-semibold">SLA politiky</h1>
                <p className="mb-6 text-sm text-slate-400">
                    Změna ovlivní jen nově vytvořené incidenty — už otevřené si drží svůj původní deadline.
                    {!isAdmin && " Úprava je jen pro roli ADMIN, tvůj účet ji má jen pro čtení."}
                </p>

                {error && (
                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                        {error}
                    </div>
                )}

                <div className="space-y-4">
                    {policies.map((policy) =>
                        isAdmin ? (
                            <form
                                key={policy.severity}
                                action={updateSlaPolicyAction.bind(null, policy.severity)}
                                className="flex flex-wrap items-end gap-4 rounded-xl border border-white/10 bg-slate-900/60 p-5"
                            >
                                <div className="min-w-[100px]">
                                    <p className="text-xs text-slate-500">Severity</p>
                                    <p className="text-lg font-semibold">{policy.severity}</p>
                                </div>
                                <label className="flex flex-col gap-1 text-xs text-slate-500">
                                    SLA (minut)
                                    <input
                                        type="number"
                                        name="slaMinutes"
                                        min={1}
                                        defaultValue={policy.slaMinutes}
                                        required
                                        className="w-28 rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm text-slate-100 focus:border-red-400 focus:outline-none"
                                    />
                                </label>
                                <label className="flex flex-col gap-1 text-xs text-slate-500">
                                    Near-breach %
                                    <input
                                        type="number"
                                        name="nearBreachPercentage"
                                        min={1}
                                        max={100}
                                        defaultValue={policy.nearBreachPercentage}
                                        required
                                        className="w-24 rounded-lg border border-white/10 bg-slate-950 px-3 py-2 text-sm text-slate-100 focus:border-red-400 focus:outline-none"
                                    />
                                </label>
                                <button
                                    type="submit"
                                    className="rounded-lg bg-red-500 px-4 py-2 text-sm font-semibold text-white hover:bg-red-400"
                                >
                                    Uložit
                                </button>
                            </form>
                        ) : (
                            <div
                                key={policy.severity}
                                className="flex flex-wrap items-end gap-4 rounded-xl border border-white/10 bg-slate-900/60 p-5"
                            >
                                <div className="min-w-[100px]">
                                    <p className="text-xs text-slate-500">Severity</p>
                                    <p className="text-lg font-semibold">{policy.severity}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-slate-500">SLA (minut)</p>
                                    <p className="text-sm text-slate-200">{policy.slaMinutes}</p>
                                </div>
                                <div>
                                    <p className="text-xs text-slate-500">Near-breach %</p>
                                    <p className="text-sm text-slate-200">{policy.nearBreachPercentage}</p>
                                </div>
                            </div>
                        ),
                    )}
                </div>
            </main>
        </div>
    );
}
