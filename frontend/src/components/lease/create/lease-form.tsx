"use client";

import { Building2, FileText, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { FormSection } from "@/components/lease/create/form-section";
import { LeaseSummary } from "@/components/lease/create/lease-summary";
import { LeaseTermsFields } from "@/components/lease/create/lease-terms-fields";
import {
  PropertySelect,
  type Property,
} from "@/components/lease/create/property-select";
import { TenantFields } from "@/components/lease/create/tenant-fields";
import {
  createLeaseSchema,
  type CreateLeaseDto,
  type CreateLeaseValues,
} from "@/lib/schema/leaseSchema";

type LeaseFormProps = {
  properties: Property[];
  action: (data: CreateLeaseDto) => void | Promise<void>;
};

export function LeaseForm({ properties, action }: LeaseFormProps) {
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<CreateLeaseValues, undefined, CreateLeaseDto>({
    resolver: zodResolver(createLeaseSchema),
    defaultValues: {
      propertyId: "",
      tenantEmail: "",
      startDate: "",
    },
  });

  const propertyId = watch("propertyId");
  const tenantEmail = watch("tenantEmail");
  const depositAmount = watch("depositAmount");
  const startDate = watch("startDate");

  const selected = properties.find((p) => p.id === propertyId);

  return (
    <form
      noValidate
      onSubmit={handleSubmit(action)}
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
            onValueChange={(id) =>
              setValue("propertyId", id, { shouldValidate: true })
            }
            error={errors.propertyId?.message}
          />
        </FormSection>

        <FormSection
          step="02"
          title="Tenant"
          description="We'll email this tenant a private invitation."
          icon={User}
        >
          <TenantFields register={register} errors={errors} />
        </FormSection>

        <FormSection
          step="03"
          title="Lease terms"
          description="Record the agreed rent, deposit, and start date."
          icon={FileText}
        >
          <LeaseTermsFields register={register} errors={errors} />
        </FormSection>
      </div>

      <LeaseSummary
        address={selected?.address}
        city={selected?.city}
        tenantEmail={tenantEmail}
        deposit={depositAmount}
        startDate={typeof startDate === "string" ? startDate : ""}
        isSubmitting={isSubmitting}
      />
    </form>
  );
}
