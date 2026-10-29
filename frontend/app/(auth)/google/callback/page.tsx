"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { getCurrentUser, homePathFor } from "@/lib/api/auth";
import {
  GoogleCallbackStatus,
  type CallbackStatus,
} from "@/components/auth/google/google-auth-callback";
import { queryKeys } from "@/lib/query-cache-key";

export default function GoogleCallbackPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<CallbackStatus>("loading");
  const started = useRef(false);

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    queryClient
      .query({ queryKey: queryKeys.me, queryFn: getCurrentUser, staleTime: 0 })
      .then((user) => {
        if (user) router.replace(homePathFor(user.role));
        else setStatus("error");
      })
      .catch(() => setStatus("error"));
  }, [queryClient, router]);

  return <GoogleCallbackStatus status={status} />;
}
