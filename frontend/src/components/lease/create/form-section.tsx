import type { LucideIcon } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

type FormSectionProps = {
  step: string;
  title: string;
  description: string;
  icon: LucideIcon;
  children: React.ReactNode;
};

export function FormSection({
  step,
  title,
  description,
  icon: Icon,
  children,
}: FormSectionProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-start gap-4">
        <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
          <Icon className="size-5" />
        </div>
        <div className="space-y-1">
          <CardTitle className="flex items-baseline gap-2">
            <span className="text-xs font-normal text-muted-foreground">
              {step}
            </span>
            {title}
          </CardTitle>
          <CardDescription>{description}</CardDescription>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">{children}</CardContent>
    </Card>
  );
}
