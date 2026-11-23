import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

type LeaseTermsFieldsProps = {
  deposit: string;
  onDepositChange: (value: string) => void;
  startDate: string;
  onStartDateChange: (value: string) => void;
};

export function LeaseTermsFields({
  deposit,
  onDepositChange,
  startDate,
  onStartDateChange,
}: LeaseTermsFieldsProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div className="space-y-2">
        <Label htmlFor="monthlyRent">Monthly rent (NPR)</Label>
        <Input
          id="monthlyRent"
          name="monthlyRent"
          type="number"
          min={0}
          placeholder="e.g. 35000"
          required
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="depositAmount">Deposit amount (NPR)</Label>
        <Input
          id="depositAmount"
          name="depositAmount"
          type="number"
          min={0}
          placeholder="e.g. 70000"
          value={deposit}
          onChange={(e) => onDepositChange(e.target.value)}
          required
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="leaseStartDate">Lease start date</Label>
        <Input
          id="leaseStartDate"
          name="leaseStartDate"
          type="date"
          value={startDate}
          onChange={(e) => onStartDateChange(e.target.value)}
          required
        />
      </div>
    </div>
  );
}
