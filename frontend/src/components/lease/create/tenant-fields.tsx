import type { FieldErrors, UseFormRegister } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { CreateLeaseValues } from "@/lib/schema/leaseSchema";

type TenantFieldsProps = {
  register: UseFormRegister<CreateLeaseValues>;
  errors: FieldErrors<CreateLeaseValues>;
};

export function TenantFields({ register, errors }: TenantFieldsProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor="tenantEmail">Tenant email</Label>
      <Input
        id="tenantEmail"
        type="email"
        placeholder="tenant@example.com"
        aria-invalid={!!errors.tenantEmail}
        {...register("tenantEmail")}
      />
      {errors.tenantEmail && (
        <p className="text-sm text-destructive">{errors.tenantEmail.message}</p>
      )}
    </div>
  );
}
