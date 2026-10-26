import {
  formatRs,
  type ActivityEntry,
  type CurrentUser,
  type DashboardStat,
  type PendingAction,
} from "@/lib/mock-data";

export const currentTenant: CurrentUser = {
  name: "Maya Gurung",
  firstName: "Maya",
  role: "Tenant",
  initials: "MG",
};

export const tenantStats: DashboardStat[] = [
  {
    id: "my-leases",
    label: "My leases",
    value: "6",
    hint: "2 currently active",
    icon: "leases",
    tone: "primary",
  },
  {
    id: "deposits-paid",
    label: "Deposits paid",
    value: formatRs(210000),
    hint: "Held securely in escrow",
    icon: "escrow",
    tone: "primary",
  },
  {
    id: "actions-needed",
    label: "Actions needed",
    value: "2",
    hint: "Complete to stay on track",
    icon: "payment",
    tone: "amber",
  },
];

export const tenantPendingActions: PendingAction[] = [
  {
    id: "tpa-1",
    kind: "payment",
    title: "Pay your security deposit",
    property: "Lazimpat Garden Residence",
    ctaLabel: "Pay deposit",
    href: "/tenant/leases/lazimpat-garden-residence",
  },
  {
    id: "tpa-2",
    kind: "photos",
    title: "Upload move-out photos",
    property: "Baluwatar Heights",
    ctaLabel: "Add photos",
    href: "/tenant/leases/baluwatar-heights",
  },
  {
    id: "tpa-3",
    kind: "settlement",
    title: "Respond to settlement",
    property: "Bhaisepati Green Homes",
    ctaLabel: "Review proposal",
    href: "/tenant/leases/bhaisepati-green-homes",
  },
];

export const tenantActivity: ActivityEntry[] = [
  {
    id: "ta-1",
    title: "Settlement agreed",
    description: "Both parties agreed on Bhaisepati Green Homes",
    time: "2 hours ago",
  },
  {
    id: "ta-2",
    title: "Photos uploaded",
    description: "Maya added 8 move-out photos",
    time: "Yesterday",
  },
  {
    id: "ta-3",
    title: "Deposit received",
    description: `${formatRs(65000)} is now held in escrow`,
    time: "3 days ago",
  },
];
