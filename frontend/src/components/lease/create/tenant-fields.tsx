import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

type TenantFieldsProps = {
  tenantEmail: string;
  onTenantEmailChange: (email: string) => void;
};

export function TenantFields({
  tenantEmail,
  onTenantEmailChange,
}: TenantFieldsProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor="tenantEmail">Tenant email</Label>
      <Input
        id="tenantEmail"
        name="tenantEmail"
        type="email"
        placeholder="tenant@example.com"
        value={tenantEmail}
        onChange={(e) => onTenantEmailChange(e.target.value)}
        required
      />
    </div>
  );
}
