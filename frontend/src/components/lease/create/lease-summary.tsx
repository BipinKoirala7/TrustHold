import { CalendarDays, MapPin, User, Wallet } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";

type LeaseSummaryProps = {
  name?: string;
  address?: string;
  city?: string;
  tenantEmail: string;
  deposit?: number;
  startDate: string;
  isPending: boolean;
};

function SummaryRow({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-start gap-3">
      <Icon className="mt-0.5 size-4 shrink-0 text-primary" />
      <div className="min-w-0">
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="truncate text-sm font-medium">{value}</p>
      </div>
    </div>
  );
}

export function LeaseSummary({
  name,
  address,
  city,
  tenantEmail,
  deposit,
  startDate,
  isPending,
}: LeaseSummaryProps) {
  const depositText = deposit
    ? `Rs. ${deposit.toLocaleString("en-IN")}`
    : "Not entered";

  const startText = startDate
    ? new Date(startDate).toLocaleDateString("en-GB", {
        day: "numeric",
        month: "short",
        year: "numeric",
      })
    : "Not selected";

  return (
    <Card className="lg:sticky lg:top-6">
      <CardContent className="flex flex-col gap-2">
        <p className="text-xs font-semibold uppercase tracking-wider text-primary">
          Lease summary
        </p>

        <div className="space-y-1 border-b pb-4">
          <h3 className="truncate text-lg font-semibold">
            {name || "No property selected"}
          </h3>
          {name && (
            <div className="flex items-start gap-1.5 text-sm text-muted-foreground">
              <MapPin className="mt-0.5 size-4 shrink-0" />
              <p className="min-w-0 wrap-break-word">
                {address}, {city}
              </p>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-4 border-b py-4">
          <SummaryRow
            icon={User}
            label="Tenant"
            value={tenantEmail || "Not entered"}
          />
          <SummaryRow
            icon={Wallet}
            label="Security deposit"
            value={depositText}
          />
          <SummaryRow
            icon={CalendarDays}
            label="Lease starts"
            value={startText}
          />
        </div>

        <Button type="submit" className="mt-2 w-full" disabled={isPending}>
          {isPending && <Spinner />}
          {isPending ? "Creating lease..." : "Create & invite tenant"}
        </Button>
        <p className="text-center text-xs text-muted-foreground">
          The tenant will receive an invitation by email.
        </p>
      </CardContent>
    </Card>
  );
}
