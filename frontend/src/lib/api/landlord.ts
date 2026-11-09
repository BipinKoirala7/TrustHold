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

const LANDLORD_DASHBOARD_SUMMARY_STATS_PATH = "landlord/stats";
export async function getLandlordDashboardSummaryStats() {
  const res = await api.get<ApiResponse<LandlordDashboardSummaryDto>>(
    LANDLORD_DASHBOARD_SUMMARY_STATS_PATH,
  );
  return res.data.data;
}
