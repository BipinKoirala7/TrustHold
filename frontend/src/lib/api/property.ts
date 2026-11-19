import { ApiResponse } from "./api";
import api from "./axios";

export type PropertyType = "APARTMENT" | "HOUSE" | "LAND";

export interface Property {
  id: string;
  landlordId: string;
  name: string;
  type: PropertyType;
  addressLine: string;
  city: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export type PropertySummaryDto = Omit<
  Property,
  | "id"
  | "landlordId"
  | "addressLine"
  | "description"
  | "createdAt"
  | "updatedAt"
>;

const GET_LANDLORD_PROPERTIES_PATH = "/property";
export async function getLandlordProperties(): Promise<Property[]> {
  return api
    .get<ApiResponse<Property[]>>(GET_LANDLORD_PROPERTIES_PATH)
    .then((res) => res.data.data);
}
