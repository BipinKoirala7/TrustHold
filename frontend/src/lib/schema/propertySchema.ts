import z from "zod";

export const propertyTypeSchema = z.enum(["APARTMENT", "HOUSE", "LAND"]);
export type PropertyType = z.infer<typeof propertyTypeSchema>;

export const propertyTypeLabels = {
  APARTMENT: "Apartment",
  HOUSE: "House",
  LAND: "Land",
};

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

export const createPropertySchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, "Property name is required")
    .max(100, "Property name must be at most 100 characters"),
  type: propertyTypeSchema,
  addressLine: z
    .string()
    .trim()
    .min(1, "Address is required")
    .max(200, "Address must be at most 200 characters"),
  city: z
    .string()
    .trim()
    .min(1, "City is required")
    .max(100, "City must be at most 100 characters"),
  description: z
    .string()
    .trim()
    .min(1, "Description is required")
    .max(1000, "Description must be at most 1000 characters"),
});

export type CreatePropertyDto = z.infer<typeof createPropertySchema>;

export type PropertySummaryDto = Omit<
  Property,
  | "id"
  | "landlordId"
  | "addressLine"
  | "description"
  | "createdAt"
  | "updatedAt"
>;
