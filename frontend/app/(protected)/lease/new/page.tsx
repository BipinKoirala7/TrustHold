import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { LeaseForm } from "@/components/lease/create/lease-form";

export default async function NewLeasePage() {
  return (
    <div className="mx-auto max-w-5xl space-y-6 p-6">
      <div className="space-y-4">
        <Link
          href="/landlord/leases"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back
        </Link>
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">
            Create a new lease
          </h1>
          <p className="text-muted-foreground">
            Invite one known tenant and keep property details, terms, and
            deposit activity together.
          </p>
        </div>
      </div>

      <LeaseForm />
    </div>
  );
}
