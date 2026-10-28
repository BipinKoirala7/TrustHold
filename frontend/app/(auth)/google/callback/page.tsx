"use client";

import { useAuth } from "@/lib/hooks/useAuth";
import { useRouter } from "next/navigation";

function GoogleCallBackPage() {
  const router = useRouter();
  const { user } = useAuth();
  return <div>page</div>;
}

export default GoogleCallBackPage;
