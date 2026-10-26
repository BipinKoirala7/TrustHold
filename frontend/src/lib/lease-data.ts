export type LeaseStatus =
  | "PENDING_MOVE_IN"
  | "ACTIVE"
  | "MOVE_OUT_IN_PROGRESS"
  | "MOVED_OUT"
  | "AGREED"
  | "SETTLED";

export interface Lease {
  id: string;
  propertyName: string;
  propertyDetail: string; // "Kathmandu · 3rd floor, Unit 3B"
  tenantName: string;
  deposit: number;
  status: LeaseStatus;
  startDate: string; // ISO date
}

export const leaseStatusMeta: Record<
  LeaseStatus,
  { label: string; className: string }
> = {
  PENDING_MOVE_IN: {
    label: "Pending Move In",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  ACTIVE: {
    label: "Active",
    className: "border-green-200 bg-green-50 text-green-700",
  },
  MOVE_OUT_IN_PROGRESS: {
    label: "Move Out In Progress",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  MOVED_OUT: {
    label: "Moved Out",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  AGREED: {
    label: "Agreed",
    className: "border-green-200 bg-green-50 text-green-700",
  },
  SETTLED: {
    label: "Settled",
    className: "border-green-200 bg-green-50 text-green-700",
  },
};

export const leaseStatusOptions = [
  { value: "all", label: "All statuses" },
  ...(Object.keys(leaseStatusMeta) as LeaseStatus[]).map((status) => ({
    value: status,
    label: leaseStatusMeta[status].label,
  })),
];

export const leases: Lease[] = [
  {
    id: "lazimpat-garden-residence",
    propertyName: "Lazimpat Garden Residence",
    propertyDetail: "Kathmandu · 3rd floor, Unit 3B",
    tenantName: "Maya Gurung",
    deposit: 50000,
    status: "PENDING_MOVE_IN",
    startDate: "2025-02-15",
  },
  {
    id: "jhamsikhel-courtyard-home",
    propertyName: "Jhamsikhel Courtyard Home",
    propertyDetail: "Lalitpur · West wing, 2 bedrooms",
    tenantName: "Sujan Thapa",
    deposit: 55000,
    status: "ACTIVE",
    startDate: "2025-01-10",
  },
  {
    id: "baluwatar-heights",
    propertyName: "Baluwatar Heights",
    propertyDetail: "Kathmandu · Unit 5A",
    tenantName: "Anita Rai",
    deposit: 60000,
    status: "MOVE_OUT_IN_PROGRESS",
    startDate: "2024-08-01",
  },
  {
    id: "lakeside-view-house",
    propertyName: "Lakeside View House",
    propertyDetail: "Pokhara · Ground and first floor",
    tenantName: "Prakash Adhikari",
    deposit: 65000,
    status: "MOVED_OUT",
    startDate: "2024-06-20",
  },
  {
    id: "bhaisepati-green-homes",
    propertyName: "Bhaisepati Green Homes",
    propertyDetail: "Lalitpur · Block C, House 12",
    tenantName: "Binita Karki",
    deposit: 70000,
    status: "AGREED",
    startDate: "2024-05-05",
  },
  {
    id: "sanepa-quiet-corner",
    propertyName: "Sanepa Quiet Corner",
    propertyDetail: "Lalitpur · Unit 2D",
    tenantName: "Rohan Maharjan",
    deposit: 70000,
    status: "SETTLED",
    startDate: "2023-11-12",
  },
];

export const formatDeposit = (amount: number) =>
  `Rs. ${amount.toLocaleString("en-IN")}`;

// Fixed locale + UTC so the server and browser always render the same text
export const formatLeaseDate = (iso: string) =>
  new Intl.DateTimeFormat("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    timeZone: "UTC",
  }).format(new Date(iso));
