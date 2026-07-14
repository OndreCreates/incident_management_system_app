"use client";

import { useFormStatus } from "react-dom";

interface SubmitButtonProps {
    children: React.ReactNode;
    className?: string;
    /** Only needed on a form with more than one submit button (see incidents/page.tsx's
     * bulk toolbar) -- overrides which Server Action this particular button submits to. */
    formAction?: (formData: FormData) => void;
    pendingLabel?: string;
}

/**
 * useFormStatus() only works inside a child of the <form> it reads pending state from --
 * hence this is its own client component rather than inline in the (server) page. Kept as
 * a single shared primitive so every form in the admin panel gets the same disabled/label
 * behavior during a Server Action round trip, instead of each page reinventing it.
 */
export function SubmitButton({ children, className, formAction, pendingLabel }: SubmitButtonProps) {
    const { pending } = useFormStatus();
    return (
        <button
            type="submit"
            formAction={formAction}
            disabled={pending}
            aria-busy={pending}
            className={`${className ?? ""} disabled:cursor-not-allowed disabled:opacity-60`}
        >
            {pending ? (pendingLabel ?? "Ukládám…") : children}
        </button>
    );
}
