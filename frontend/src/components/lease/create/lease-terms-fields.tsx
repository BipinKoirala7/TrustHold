import type { FieldErrors, UseFormRegister } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { CreateLeaseValues } from "@/lib/schema/leaseSchema";

type LeaseTermsFieldsProps = {
  register: UseFormRegister<CreateLeaseValues>;
  errors: FieldErrors<CreateLeaseValues>;
};

export function LeaseTermsFields({ register, errors }: LeaseTermsFieldsProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div className="space-y-2">
        <Label htmlFor="monthlyRent">Monthly rent (NPR)</Label>
        <Input
          id="monthlyRent"
          type="number"
          placeholder="e.g. 35000"
          aria-invalid={!!errors.monthlyRent}
          {...register("monthlyRent", { valueAsNumber: true })}
        />
        {errors.monthlyRent && (
          <p className="text-sm text-destructive">
            {errors.monthlyRent.message}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="depositAmount">Deposit amount (NPR)</Label>
        <Input
          id="depositAmount"
          type="number"
          placeholder="e.g. 70000"
          aria-invalid={!!errors.depositAmount}
          {...register("depositAmount", { valueAsNumber: true })}
        />
        {errors.depositAmount && (
          <p className="text-sm text-destructive">
            {errors.depositAmount.message}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="startDate">Lease start date</Label>
        <Input
          id="startDate"
          type="date"
          aria-invalid={!!errors.startDate}
          {...register("startDate")}
        />
        {errors.startDate && (
          <p className="text-sm text-destructive">{errors.startDate.message}</p>
        )}
      </div>
    </div>
  );
}
