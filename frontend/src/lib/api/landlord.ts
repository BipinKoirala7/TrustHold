import { ApiResponse } from "./api";
import api from "./axios";

export interface LandlordDashboardSummaryDto {
  activeLeases: number;
  moveOutThisMonth: number;
  totalAmountHeldInEscrow: number;
  escrowDepositCount: number;
  totalAmountAwaitingPayment: number;
  totalPendingAmount: number;
  numbOfAwaitingMyAgreement: number;
}

export interface LeaseActivity {
  id: string;
  leaseId: string;
  activity: string;
  description: string;
  createdAt: string;
}

const LANDLORD_DASHBOARD_SUMMARY_STATS_PATH = "landlord/stats";
export async function getLandlordDashboardSummaryStats() {
  const res = await api.get<ApiResponse<LandlordDashboardSummaryDto>>(
    LANDLORD_DASHBOARD_SUMMARY_STATS_PATH,
  );
  return res.data.data;
}

const LANDLORD_RECENT_ACTIVITY_PATH = "landlord/recent-activity";
export async function getLandlordRecentActivity() {
  const res = await api.get<ApiResponse<LeaseActivity[]>>(
    LANDLORD_RECENT_ACTIVITY_PATH,
  );
  return res.data.data;
}
