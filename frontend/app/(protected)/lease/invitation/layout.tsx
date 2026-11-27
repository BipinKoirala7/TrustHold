import TenantProtectedLayout from "@/components/TenantProtectedLayout";

export default function LeaseInvitationLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <TenantProtectedLayout>{children}</TenantProtectedLayout>;
}
