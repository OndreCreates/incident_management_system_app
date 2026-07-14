import { fetchMe, fetchSlaPolicies, fetchSlaPolicyHistory } from "@/lib/api";
import { requireSession } from "@/lib/auth";
import { Nav } from "@/components/Nav";
import { SubmitButton } from "@/components/SubmitButton";
import { updateSlaPolicyAction } from "@/app/sla-policies/actions";

interface SlaPoliciesPageProps {
    searchParams: Promise<{ error?: string }>;
}

export default async function SlaPoliciesPage({ searchParams }: SlaPoliciesPageProps) {
    const session = await requireSession("/sla-policies");
    const { error } = await searchParams;
    const [policies, me, history] = await Promise.all([
        fetchSlaPolicies(session.accessToken),
        fetchMe(session.accessToken),
        fetchSlaPolicyHistory(session.accessToken),
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
                                <SubmitButton className="rounded-lg bg-red-500 px-4 py-2 text-sm font-semibold text-white hover:bg-red-400">
                                    Uložit
                                </SubmitButton>
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

                <h2 className="mb-3 mt-10 text-lg font-semibold">Historie změn</h2>
                <div className="overflow-hidden rounded-xl border border-white/10">
                    <table className="w-full text-left text-sm">
                        <thead className="bg-slate-900/60 text-xs uppercase tracking-wide text-slate-500">
                            <tr>
                                <th className="px-4 py-3">Kdy</th>
                                <th className="px-4 py-3">Severity</th>
                                <th className="px-4 py-3">SLA (min)</th>
                                <th className="px-4 py-3">Near-breach %</th>
                                <th className="px-4 py-3">Kdo</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-white/5">
                            {history.map((change) => (
                                <tr key={change.id}>
                                    <td className="px-4 py-3 text-slate-400">
                                        {new Date(change.changedAt).toLocaleString("cs-CZ")}
                                    </td>
                                    <td className="px-4 py-3">{change.severity}</td>
                                    <td className="px-4 py-3 text-slate-300">
                                        {change.oldSlaMinutes} → {change.newSlaMinutes}
                                    </td>
                                    <td className="px-4 py-3 text-slate-300">
                                        {change.oldNearBreachPercentage} → {change.newNearBreachPercentage}
                                    </td>
                                    <td className="px-4 py-3 text-slate-400">{change.changedBy}</td>
                                </tr>
                            ))}
                            {history.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="px-4 py-8 text-center text-slate-500">
                                        Zatím žádná změna.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    );
}
