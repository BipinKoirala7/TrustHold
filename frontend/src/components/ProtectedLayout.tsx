"use client";

import { useAuth } from "@/lib/hooks/useAuth";
import { redirect, usePathname } from "next/navigation";
import { Spinner } from "./ui/spinner";
import { Role } from "@/lib/schema/authSchema";
import { homePathFor } from "@/lib/api/auth";

const ROLE_PREFIXES: Record<string, Role> = {
  "/tenant": "TENANT",
  "/landlord": "LANDLORD",
  "/admin": "ADMIN",
};

export default function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, isLoading } = useAuth();
  const path = usePathname();

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

  const requiredRole = Object.entries(ROLE_PREFIXES).find(
    ([prefix]) => path === prefix || path.startsWith(`${prefix}/`),
  )?.[1];

  if (requiredRole && user.role !== requiredRole) {
    redirect(homePathFor(user.role));
  }

  return <>{children}</>;
}
