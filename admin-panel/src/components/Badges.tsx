import type { Severity, Status } from "@/lib/types";

const STATUS_STYLES: Record<Status, string> = {
    CREATED: "bg-slate-500/15 text-slate-300 border-slate-500/30",
    ASSIGNED: "bg-sky-500/15 text-sky-300 border-sky-500/30",
    INVESTIGATING: "bg-amber-500/15 text-amber-300 border-amber-500/30",
    MITIGATED: "bg-violet-500/15 text-violet-300 border-violet-500/30",
    RESOLVED: "bg-emerald-500/15 text-emerald-300 border-emerald-500/30",
    CLOSED: "bg-slate-700/40 text-slate-400 border-slate-600/40",
};

const SEVERITY_STYLES: Record<Severity, string> = {
    CRITICAL: "bg-red-500/15 text-red-300 border-red-500/30",
    HIGH: "bg-orange-500/15 text-orange-300 border-orange-500/30",
    MEDIUM: "bg-amber-500/15 text-amber-300 border-amber-500/30",
    LOW: "bg-slate-500/15 text-slate-300 border-slate-500/30",
};

function Badge({ className, children }: { className: string; children: React.ReactNode }) {
    return (
        <span
            className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium ${className}`}
        >
            {children}
        </span>
    );
}

export function StatusBadge({ status }: { status: Status }) {
    return <Badge className={STATUS_STYLES[status]}>{status}</Badge>;
}

export function SeverityBadge({ severity }: { severity: Severity }) {
    return <Badge className={SEVERITY_STYLES[severity]}>{severity}</Badge>;
}

export function BreachedBadge() {
    return (
        <Badge className="border-red-500/40 bg-red-500/20 text-red-300">
            SLA porušeno
        </Badge>
    );
}
