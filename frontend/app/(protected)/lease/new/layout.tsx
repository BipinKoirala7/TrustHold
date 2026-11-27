import LandlordProtectedLayout from "@/components/LandlordProtectedLayout";

export default function NewLeaseLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <LandlordProtectedLayout>{children}</LandlordProtectedLayout>;
}
