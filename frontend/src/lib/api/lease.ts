import { ApiResponse } from "@/lib/api/api";
import api from "./axios";
import {
  CreateLeaseDto,
  LeaseStatus,
  leaseStatusMeta,
  LeaseSummaryDto,
} from "../schema/leaseSchema";

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

const CREATE_LEASE_PATH = "/lease";
export async function createLease(lease: CreateLeaseDto): Promise<void> {
  await api.post<ApiResponse<void>>(CREATE_LEASE_PATH, lease);
}

const GET_LEASES_PATH = "/lease";
export async function getLeases(): Promise<LeaseSummaryDto[]> {
  const res = await api.get<ApiResponse<LeaseSummaryDto[]>>(GET_LEASES_PATH);
  return res.data.data;
}
