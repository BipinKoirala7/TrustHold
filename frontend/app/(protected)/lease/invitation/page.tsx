import { AcceptInvitationCard } from "@/components/lease/invitation/AcceptInvitationCard";

export default async function LeaseInvitationPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  const { token } = await searchParams;

  return (
    <main className="flex min-h-screen items-center justify-center p-4">
      <AcceptInvitationCard token={token} />
    </main>
  );
}
