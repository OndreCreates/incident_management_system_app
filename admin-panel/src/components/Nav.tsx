import Link from "next/link";

export function Nav() {
    return (
        <header className="border-b border-white/10 bg-slate-950/80 backdrop-blur">
            <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
                <Link href="/incidents" className="text-sm font-semibold tracking-wide text-slate-100">
                    OndreCreates <span className="text-slate-500">/</span> Incident Management
                </Link>
                <nav className="flex items-center gap-6 text-sm text-slate-400">
                    <Link href="/incidents" className="hover:text-slate-100">
                        Incidenty
                    </Link>
                    <Link href="/dashboard" className="hover:text-slate-100">
                        Dashboard
                    </Link>
                    <Link href="/teams" className="hover:text-slate-100">
                        Týmy
                    </Link>
                    <Link
                        href="/incidents/new"
                        className="rounded-lg bg-red-500 px-3 py-1.5 font-medium text-white hover:bg-red-400"
                    >
                        + Nový incident
                    </Link>
                    <a href="/logout" className="hover:text-slate-100">
                        Odhlásit se
                    </a>
                </nav>
            </div>
        </header>
    );
}
