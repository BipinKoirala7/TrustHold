import { CalendarDays, User, Wallet } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

type LeaseSummaryProps = {
  address?: string;
  city?: string;
  tenantEmail: string;
  deposit?: number;
  startDate: string;
  isSubmitting: boolean;
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
      <Icon className="mt-0.5 size-4 text-primary" />
      <div className="min-w-0">
        <p className="text-xs text-muted-foreground">{label}</p>
        <p className="truncate text-sm font-medium">{value}</p>
      </div>
    </div>
  );
}

export function LeaseSummary({
  address,
  city,
  tenantEmail,
  deposit,
  startDate,
  isSubmitting,
}: LeaseSummaryProps) {
  // an empty number input gives NaN, which is falsy, so it shows "Not entered"
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
      <CardContent className="space-y-4">
        <p className="text-xs font-semibold uppercase tracking-wider text-primary">
          Lease summary
        </p>

        <div className="space-y-1 border-b pb-4">
          <h3 className="text-lg font-semibold">
            {address || "Property address"}
          </h3>
          <p className="text-sm text-muted-foreground">
            {city || "No property selected"}
          </p>
        </div>

        <div className="space-y-4 border-b pb-4">
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

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          Create &amp; invite tenant
        </Button>
        <p className="text-center text-xs text-muted-foreground">
          The tenant will receive an invitation by email.
        </p>
      </CardContent>
    </Card>
  );
}
