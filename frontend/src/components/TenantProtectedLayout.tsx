"use client";

import { useAuth } from "@/hooks/useAuth";
import { redirect } from "next/navigation";
import { Spinner } from "./ui/spinner";
import { homePathFor } from "@/lib/api/auth";

function TenantProtectedLayout({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="w-full h-screen flex items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!user) {
    redirect("/login");
  }

  if (user.role !== "TENANT") {
    redirect(homePathFor(user.role));
  }

  return <div>{children}</div>;
}

export default TenantProtectedLayout;
