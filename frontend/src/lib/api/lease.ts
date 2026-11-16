import { ApiResponse } from "@/lib/api/api";
import api from "./axios";
import { PropertySummaryDto } from "@/lib/api/property";

export type LeaseStatus =
  | "PENDING_TENANT"
  | "PENDING_MOVE_IN"
  | "PENDING_DEPOSIT"
  | "ACTIVE"
  | "MOVE_OUT_IN_PROGRESS"
  | "PENDING_SETTLEMENT"
  | "SETTLEMENT_AGREED"
  | "CANCELLED"
  | "MOVED_OUT";

export interface Lease {
  id: string;
  version: number;
  propertyId: string;
  landlordId: string;
  tenantId: string | null;
  monthlyRent: number;
  depositAmount: number;
  startDate: string;
  endDate: string;
  status: LeaseStatus;
  movedInAt: string | null;
  moveOutInitiatedAt: string | null;
  moveOutInitiatedBy: string | null;
  movedOutAt: string | null;
  settled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LeaseSummaryDto {
  id: string;
  property: PropertySummaryDto;
  tenantName: string;
  depositAmount: number;
  status: LeaseStatus;
  startDate: string;
}

export const leaseStatusMeta: Record<
  LeaseStatus,
  { label: string; className: string }
> = {
  PENDING_TENANT: {
    label: "Pending Tenant",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  PENDING_MOVE_IN: {
    label: "Pending Move In",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  PENDING_DEPOSIT: {
    label: "Pending Deposit",
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
  PENDING_SETTLEMENT: {
    label: "Pending Settlement",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  SETTLEMENT_AGREED: {
    label: "Agreed",
    className: "border-green-200 bg-green-50 text-green-700",
  },
  CANCELLED: {
    label: "Cancelled",
    className: "border-red-200 bg-red-50 text-red-700",
  },
  MOVED_OUT: {
    label: "Moved Out",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
};

export const leaseStatusOptions = [
  { value: "all", label: "All statuses" },
  ...(Object.keys(leaseStatusMeta) as LeaseStatus[]).map((status) => ({
    value: status,
    label: leaseStatusMeta[status].label,
  })),
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

// API functions
const GET_LEASES_PATH = "/lease";
export async function getLeases(): Promise<LeaseSummaryDto[]> {
  const res = await api.get<ApiResponse<LeaseSummaryDto[]>>(GET_LEASES_PATH);
  return res.data.data;
}
