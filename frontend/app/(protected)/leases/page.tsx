import { LeasesView } from "@/components/lease/lease-view";

export default function LandlordLeasesPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Leases</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          View your invitations, deposits, and lease progress.
        </p>
      </div>
      <LeasesView />
    </div>
  );
}
