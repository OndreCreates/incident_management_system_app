"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { DailyCount } from "@/lib/types";

/** Single series (incidents created per day) -- per dataviz guidance, a lone series needs no
 * legend box, the chart title already names it. Thin bars, recessive grid, hover tooltip
 * instead of a label on every bar. Red-500 matches the app's existing accent (buttons,
 * badges) rather than introducing a second brand hue just for this one chart. */
export function TrendChart({ data }: { data: DailyCount[] }) {
    const chartData = data.map((d) => ({ ...d, label: formatShortDate(d.date) }));

    return (
        <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" vertical={false} />
                    <XAxis
                        dataKey="label"
                        tick={{ fill: "#64748b", fontSize: 11 }}
                        axisLine={{ stroke: "rgba(255,255,255,0.1)" }}
                        tickLine={false}
                    />
                    <YAxis
                        allowDecimals={false}
                        tick={{ fill: "#64748b", fontSize: 11 }}
                        axisLine={false}
                        tickLine={false}
                        width={28}
                    />
                    <Tooltip
                        cursor={{ fill: "rgba(255,255,255,0.04)" }}
                        contentStyle={{
                            background: "#0f172a",
                            border: "1px solid rgba(255,255,255,0.1)",
                            borderRadius: 8,
                            fontSize: 12,
                        }}
                        labelStyle={{ color: "#e2e8f0" }}
                        itemStyle={{ color: "#f87171" }}
                        formatter={(value) => [value ?? 0, "Vytvořeno"]}
                    />
                    <Bar dataKey="count" fill="#ef4444" radius={[4, 4, 0, 0]} maxBarSize={28} />
                </BarChart>
            </ResponsiveContainer>
        </div>
    );
}

function formatShortDate(iso: string): string {
    const date = new Date(iso);
    return date.toLocaleDateString("cs-CZ", { day: "numeric", month: "numeric" });
}
