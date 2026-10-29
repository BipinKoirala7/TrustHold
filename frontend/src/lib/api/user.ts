import api from "./axios";
import type { UpdateUserDto } from "@/lib/schema/userSchema";

const UPDATE_USER_PATH = "user";
export async function updateUser(profile: UpdateUserDto) {
  await api.put(UPDATE_USER_PATH, profile);
}
