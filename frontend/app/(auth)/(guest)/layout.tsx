"use client";

import { Spinner } from "@/components/ui/spinner";
import { homePathFor } from "@/lib/api/auth";
import { useAuth } from "@/lib/hooks/useAuth";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function guestLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (user) router.replace(homePathFor(user.role));
  }, [user, router]);

  if (isLoading || user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <Spinner className="h-5 w-5 animate-spin text-gray-500" />
      </div>
    );
  }

  return <>{children}</>;
}
