"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { FileText } from "lucide-react";
import { Button } from "@/components/ui/button";

function getGreeting(hour: number) {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

export function GreetingHeader({ firstName }: { firstName: string }) {
  // Computed after mount so server and client render the same HTML
  const [greeting, setGreeting] = useState("Welcome back");

  useEffect(() => {
    setGreeting(getGreeting(new Date().getHours()));
  }, []);

  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {greeting}, {firstName}
        </h1>
        <p className="mt-2 text-muted-foreground">
          Here&apos;s what&apos;s happening across your leases today.
        </p>
      </div>
      <Button>
        <Link href="/landlord/leases" className="flex gap-2">
          <FileText className="size-4" />
          View all leases
        </Link>
      </Button>
    </div>
  );
}
