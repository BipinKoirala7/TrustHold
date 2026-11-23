import { z } from "zod";
import { PropertySummaryDto } from "./propertySchema";

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

export type LeaseSummaryDto = Omit<
  Lease,
  | "landlordId"
  | "tenantId"
  | "movedInAt"
  | "moveOutInitiatedAt"
  | "moveOutInitiatedBy"
  | "movedOutAt"
  | "settled"
> & {
  property: PropertySummaryDto;
};

export interface LeaseCreateDto {
  propertyId: string;
  tenantEmail: string;
  monthlyRent: number;
  depositAmount: number;
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

export const createLeaseSchema = z.object({
  propertyId: z.string().min(1, "Property is required"),
  tenantEmail: z
    .string()
    .nonempty("Tenant email is required")
    .pipe(z.email("Invalid email address")),
  monthlyRent: z
    .number("Monthly rent must be a number")
    .min(0, "Monthly rent must be greater than or equal to 0"),
  depositAmount: z
    .number("Deposit amount must be a number")
    .min(0, "Deposit amount must be greater than or equal to 0"),
  startDate: z.coerce
    .date("Start date must be a valid date")
    .min(new Date(), "Start date must be in the future"),
});
