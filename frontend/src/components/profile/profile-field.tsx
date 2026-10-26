import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface ProfileFieldProps extends React.ComponentProps<"input"> {
  id: string;
  label: string;
}

export function ProfileField({ id, label, ...props }: ProfileFieldProps) {
  return (
    <div className="grid gap-2">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} {...props} />
    </div>
  );
}
