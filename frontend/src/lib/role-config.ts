import { currentLandlord, type CurrentUser } from "@/lib/mock-data";
import { currentTenant } from "@/lib/tenant-data";

export type Role = "landlord" | "tenant";

export const roleUsers: Record<Role, CurrentUser> = {
  landlord: currentLandlord,
  tenant: currentTenant,
};

// UI-only phase: the role comes from the URL.
// Later, replace this with the role from the logged-in session.
export const getRoleFromPath = (pathname: string): Role =>
  pathname.startsWith("/tenant") ? "tenant" : "landlord";
