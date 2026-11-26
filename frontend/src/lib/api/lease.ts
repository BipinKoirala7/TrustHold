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

const ACCEPT_LEASE_INVITATION = "/lease/invitation/accept";
export async function acceptLeaseInvitation(
  invitationToken: string,
): Promise<void> {
  await api.post<ApiResponse<void>>(ACCEPT_LEASE_INVITATION, {
    invitationToken,
  });
}
