"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { FileText } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";

function getGreeting(hour: number) {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

interface GreetingHeaderProps {
  firstName: string;
  subtitle: string;
  leasesHref: string;
  /** Fixed greeting such as "Welcome back". Leave out for a time-based one. */
  prefix?: string;
}

export function GreetingHeader({
  firstName,
  subtitle,
  leasesHref,
  prefix,
}: GreetingHeaderProps) {
  const [timeGreeting, setTimeGreeting] = useState("Welcome back");

  useEffect(() => {
    if (!prefix) setTimeGreeting(getGreeting(new Date().getHours()));
  }, [prefix]);

  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">
          {prefix ?? timeGreeting}, {firstName}
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>
      </div>
      <Link href={leasesHref} className={buttonVariants()}>
        <FileText data-icon="inline-start" />
        View all leases
      </Link>
    </div>
  );
}
