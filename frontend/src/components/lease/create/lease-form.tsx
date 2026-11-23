"use client";

import { useState } from "react";
import { Building2, FileText, User } from "lucide-react";
import { FormSection } from "@/components/lease/create/form-section";
import { LeaseSummary } from "@/components/lease/create/lease-summary";
import { LeaseTermsFields } from "@/components/lease/create/lease-terms-fields";
import {
  PropertySelect,
  type Property,
} from "@/components/lease/create/property-select";
import { TenantFields } from "@/components/lease/create/tenant-fields";

type LeaseFormProps = {
  properties: Property[];
  action: (formData: FormData) => void | Promise<void>;
};

export function LeaseForm({ properties, action }: LeaseFormProps) {
  const [propertyId, setPropertyId] = useState("");
  const [tenantEmail, setTenantEmail] = useState("");
  const [deposit, setDeposit] = useState("");
  const [startDate, setStartDate] = useState("");

  const selected = properties.find((p) => p.id === propertyId);

  return (
    <form
      action={action}
      className="grid items-start gap-6 lg:grid-cols-[1fr_320px]"
    >
      <div className="space-y-6">
        <FormSection
          step="01"
          title="Property details"
          description="Choose the property this lease is for."
          icon={Building2}
        >
          <PropertySelect
            properties={properties}
            value={propertyId}
            onValueChange={setPropertyId}
          />
        </FormSection>

        <FormSection
          step="02"
          title="Tenant"
          description="We'll email this tenant a private invitation."
          icon={User}
        >
          <TenantFields
            tenantEmail={tenantEmail}
            onTenantEmailChange={setTenantEmail}
          />
        </FormSection>

        <FormSection
          step="03"
          title="Lease terms"
          description="Record the agreed rent, deposit, and start date."
          icon={FileText}
        >
          <LeaseTermsFields
            deposit={deposit}
            onDepositChange={setDeposit}
            startDate={startDate}
            onStartDateChange={setStartDate}
          />
        </FormSection>
      </div>

      <LeaseSummary
        address={selected?.address}
        city={selected?.city}
        tenantEmail={tenantEmail}
        deposit={deposit}
        startDate={startDate}
      />
    </form>
  );
}
