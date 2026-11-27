"use client";

import Link from "next/link";
import { buttonVariants, Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useAcceptLeaseInvitation } from "@/hooks/useAcceptLeaseInvitation";

export function AcceptInvitationCard({ token }: { token?: string }) {
  const { mutate, isPending, isSuccess } = useAcceptLeaseInvitation();

  if (!token) {
    return (
      <Card className="w-full max-w-md text-center">
        <CardHeader>
          <CardTitle className="text-xl">Invalid invitation link</CardTitle>
          <CardDescription>
            This link is missing its invitation token. Open the link from your
            email again, or ask your landlord to send a new invitation.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  if (isSuccess) {
    return (
      <Card className="w-full max-w-md text-center">
        <CardHeader>
          <CardTitle className="text-xl">Invitation accepted</CardTitle>
          <CardDescription>You have joined the lease.</CardDescription>
        </CardHeader>
        <CardContent>
          <Link
            href="/tenant"
            className={buttonVariants({ className: "w-full" })}
          >
            Go to dashboard
          </Link>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full max-w-md text-center">
      <CardHeader>
        <CardTitle className="text-xl">
          You&apos;ve been invited to a lease
        </CardTitle>
        <CardDescription>
          Accept to join this lease on TrustHold. The security deposit is held
          safely, and move-in and move-out photos are kept on record.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Button
          className="w-full"
          disabled={isPending}
          onClick={() => mutate(token)}
        >
          {isPending ? "Accepting..." : "Accept invitation"}
        </Button>
      </CardContent>
    </Card>
  );
}
