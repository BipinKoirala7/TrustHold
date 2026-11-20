import LandlordProtectedLayout from "@/components/LandlordProtectedLayout";

function LandlordLayout({ children }: { children: React.ReactNode }) {
  return <LandlordProtectedLayout>{children}</LandlordProtectedLayout>;
}

export default LandlordLayout;
