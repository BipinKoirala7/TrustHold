"use client";

import Link from "next/link";
import { Button, buttonVariants } from "../ui/button";
import { Input } from "../ui/input";
import { cn } from "cn";
import { useState } from "react";
import { forgotPassword } from "@/lib/api/auth";
import { toast } from "sonner";
import { useMutation } from "@tanstack/react-query";
import { Spinner } from "../ui/spinner";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

export function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const { mutate, isPending } = useMutation({
    mutationFn: async (email: string) => {
      await forgotPassword(email);
    },
    onSuccess: () => {
      toast.success("Reset link sent successfully.", {
        description: "Please check your email for the reset link.",
      });
      email && setEmail("");
    },
    onError: (error) => {
      toast.error("Failed to send reset link. Please try again.", {
        description: error instanceof Error ? error.message : "Unknown error",
      });
    },
  });
  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <label htmlFor="email" className="text-sm font-medium text-foreground">
          Email
        </label>
        <Input
          id="email"
          placeholder="your.email@example.com"
          className={inputClass}
          onChange={(e) => setEmail(e.target.value)}
          value={email}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Button onClick={() => mutate(email)} disabled={isPending}>
          {isPending ? (
            <>
              <Spinner className="mr-2 h-4 w-4 animate-spin" />
              Sending...
            </>
          ) : (
            "Send Reset Link"
          )}
        </Button>
        <Link
          href="/login"
          className={cn(buttonVariants({ variant: "secondary" }), "w-full")}
        >
          Back to Login
        </Link>
      </div>
    </div>
  );
}
