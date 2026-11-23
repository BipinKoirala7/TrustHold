import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { LeaseForm } from "@/components/lease/create/lease-form";
import type { Property } from "@/components/lease/create/property-select";

// TODO: replace with a real call to the Spring Boot API
async function getLandlordProperties(): Promise<Property[]> {
  return [];
  // Try this to see the select:
  // return [
  //   { id: "1", address: "Lazimpat Garden Residence", city: "Kathmandu", propertyType: "Apartment", unitDescription: "3rd floor, Unit 3B" },
  // ]
}

async function createLease(formData: FormData) {
  "use server";
  // TODO: send formData to the backend, then redirect to the new lease
  console.log(Object.fromEntries(formData));
}

export default async function NewLeasePage() {
  const properties = await getLandlordProperties();

  return (
    <div className="mx-auto max-w-5xl space-y-6 p-6">
      <div className="space-y-4">
        <Link
          href="/landlord/leases"
          className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Back
        </Link>
        <div className="space-y-1">
          <h1 className="text-3xl font-bold tracking-tight">
            Create a new lease
          </h1>
          <p className="text-muted-foreground">
            Invite one known tenant and keep property details, terms, and
            deposit activity together.
          </p>
        </div>
      </div>

      <LeaseForm properties={properties} action={createLease} />
    </div>
  );
}
