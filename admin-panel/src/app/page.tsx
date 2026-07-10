import Link from "next/link";
import { getSession } from "@/lib/session";

interface HomeProps {
    searchParams: Promise<{ error?: string }>;
}

export default async function Home({ searchParams }: HomeProps) {
    const { error } = await searchParams;
    const session = await getSession();

    return (
        <main className="flex min-h-screen flex-1 items-center justify-center bg-gradient-to-b from-slate-950 to-slate-900 px-4 text-slate-100">
            <div className="w-full max-w-md rounded-2xl border border-white/10 bg-slate-900/80 p-10 shadow-2xl">
                <p className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-500">
                    OndreCreates &middot; Incident Management
                </p>
                <h1 className="mb-3 text-2xl font-semibold">Admin panel</h1>
                <p className="mb-8 text-sm leading-relaxed text-slate-400">
                    Správa incidentů, SLA a audit trail. Přihlášení probíhá přes{" "}
                    <code className="text-slate-300">identity_server_app</code>.
                </p>

                {error && (
                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                        {error}
                    </div>
                )}

                {session ? (
                    <Link
                        href="/incidents"
                        className="block w-full rounded-lg bg-red-500 px-4 py-3 text-center text-sm font-semibold text-white transition hover:bg-red-400"
                    >
                        Zobrazit incidenty
                    </Link>
                ) : (
                    <a
                        href="/login"
                        className="block w-full rounded-lg bg-red-500 px-4 py-3 text-center text-sm font-semibold text-white transition hover:bg-red-400"
                    >
                        Přihlásit se přes Identity Server
                    </a>
                )}
            </div>
        </main>
    );
}
