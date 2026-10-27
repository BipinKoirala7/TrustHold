import { Building2, User, type LucideIcon } from "lucide-react";
import type { RegistrationRole } from "@/lib/schema/authSchema";
import { cn } from "@/lib/utils";

const options: {
  value: RegistrationRole;
  label: string;
  description: string;
  icon: LucideIcon;
}[] = [
  {
    value: "LANDLORD",
    label: "Landlord",
    description: "I rent out a property",
    icon: Building2,
  },
  {
    value: "TENANT",
    label: "Tenant",
    description: "I'm renting a home",
    icon: User,
  },
];

type RoleSelectorProps = {
  value: RegistrationRole;
  onChange: (value: RegistrationRole) => void;
  name?: string;
  disabled?: boolean;
  className?: string;
};

export function RoleSelector({
  value,
  onChange,
  name = "role",
  disabled,
  className,
}: RoleSelectorProps) {
  return (
    <fieldset className={className} disabled={disabled}>
      <legend className="mb-2 text-sm font-medium text-foreground">
        I am a…
      </legend>

      <div className="grid grid-cols-2 gap-3">
        {options.map(
          ({ value: optionValue, label, description, icon: Icon }) => (
            <label key={optionValue} className="cursor-pointer">
              {/* Native radio keeps keyboard and screen-reader behaviour for free. */}
              <input
                type="radio"
                name={name}
                value={optionValue}
                checked={value === optionValue}
                onChange={() => onChange(optionValue)}
                className="peer sr-only"
              />
              <div
                className={cn(
                  "flex h-full flex-col gap-0.5 rounded-2xl border border-border bg-card p-4 transition-colors",
                  "hover:border-primary/40",
                  "peer-checked:border-primary peer-checked:ring-2 peer-checked:ring-primary/25",
                  "peer-focus-visible:ring-2 peer-focus-visible:ring-ring",
                  "peer-disabled:cursor-not-allowed peer-disabled:opacity-60",
                )}
              >
                <Icon className="mb-2 size-5 text-primary" aria-hidden />
                <span className="text-base font-semibold text-foreground">
                  {label}
                </span>
                <span className="text-sm text-muted-foreground">
                  {description}
                </span>
              </div>
            </label>
          ),
        )}
      </div>
    </fieldset>
  );
}
