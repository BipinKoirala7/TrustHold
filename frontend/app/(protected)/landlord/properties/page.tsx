import PropertiesPage from "@/components/properties/properties-page";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Properties",
  description: "Manage your properties",
};

function page() {
  return <PropertiesPage />;
}

export default page;
