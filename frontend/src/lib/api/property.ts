export type PropertyType = "APARTMENT" | "HOUSE" | "LAND";

export interface PropertySummaryDto {
  name: string;
  type: PropertyType;
  city: string;
}
