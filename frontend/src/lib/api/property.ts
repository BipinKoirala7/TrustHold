import {
  CreatePropertyDto,
  Property,
  PropertyDetailsDto,
} from "../schema/propertySchema";
import { ApiResponse } from "./api";
import api from "./axios";

const CREATE_PROPERTY_PATH = "/property";
export async function createProperty(
  property: CreatePropertyDto,
): Promise<void> {
  await api.post<ApiResponse<Property>>(CREATE_PROPERTY_PATH, property);
}

const GET_LANDLORD_PROPERTIES_PATH = "/property";
export async function getLandlordProperties(): Promise<PropertyDetailsDto[]> {
  return await api
    .get<ApiResponse<PropertyDetailsDto[]>>(GET_LANDLORD_PROPERTIES_PATH)
    .then((res) => res.data.data);
}

const DELETE_PROPERTY_PATH = "/property";
export async function deleteProperty(id: string): Promise<void> {
  await api.delete<ApiResponse<void>>(`${DELETE_PROPERTY_PATH}/${id}`);
}
