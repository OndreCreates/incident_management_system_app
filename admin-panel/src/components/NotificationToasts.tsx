"use client";

import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

interface Toast {
    id: number;
    subject: string;
    body: string;
}

interface InAppNotificationPayload {
    notificationId: number;
    subject: string;
    body: string;
}

const WS_URL = process.env.NEXT_PUBLIC_NOTIFICATION_WS_URL;
const CLIENT_ID = process.env.NEXT_PUBLIC_NOTIFICATION_CLIENT_ID;
const TOAST_LIFETIME_MS = 8000;

/**
 * Connects straight from the browser to notification_center_app's STOMP
 * broker (verified live: native ws://.../ws endpoint, no SockJS -- see
 * WebSocketConfig there) and subscribes to this app's own client topic.
 * That topic is scoped by notification_center_app's client id, not by
 * end user, so every admin panel tab sees every WEBSOCKET notification
 * this backend sends -- acceptable for this portfolio's single-admin
 * scope. Silently disabled (no connection attempt) when the env vars
 * aren't set, same "optional infrastructure" posture as escalation email.
 */
export function NotificationToasts() {
    const [toasts, setToasts] = useState<Toast[]>([]);

    useEffect(() => {
        if (!WS_URL || !CLIENT_ID) {
            return;
        }

        const client = new Client({
            brokerURL: WS_URL,
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe(`/topic/notifications/${CLIENT_ID}`, (message) => {
                    const payload = JSON.parse(message.body) as InAppNotificationPayload;
                    const toast: Toast = { id: payload.notificationId, subject: payload.subject, body: payload.body };
                    setToasts((prev) => [...prev, toast]);
                    setTimeout(() => dismiss(toast.id), TOAST_LIFETIME_MS);
                });
            },
        });
        client.activate();

        function dismiss(id: number) {
            setToasts((prev) => prev.filter((toast) => toast.id !== id));
        }

        return () => {
            client.deactivate();
        };
    }, []);

    if (toasts.length === 0) {
        return null;
    }

    return (
        <div className="fixed bottom-4 right-4 z-50 flex w-80 flex-col gap-2">
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    className="rounded-lg border border-red-500/30 bg-slate-900 px-4 py-3 shadow-lg shadow-black/30"
                >
                    <div className="flex items-start justify-between gap-2">
                        <p className="text-sm font-medium text-slate-100">{toast.subject}</p>
                        <button
                            type="button"
                            onClick={() => setToasts((prev) => prev.filter((t) => t.id !== toast.id))}
                            className="text-slate-500 hover:text-slate-300"
                            aria-label="Zavřít"
                        >
                            ×
                        </button>
                    </div>
                    <p className="mt-1 text-xs text-slate-400">{toast.body}</p>
                </div>
            ))}
        </div>
    );
}
