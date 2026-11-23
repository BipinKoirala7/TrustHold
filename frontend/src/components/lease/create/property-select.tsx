import Link from "next/link";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export type Property = {
  id: string;
  address: string;
  city: string;
  propertyType: string;
  unitDescription?: string;
};

type PropertySelectProps = {
  properties: Property[];
  value: string;
  onValueChange: (id: string) => void;
};

function propertyLabel(property: Property) {
  return `${property.address}, ${property.city}${
    property.unitDescription ? ` · ${property.unitDescription}` : ""
  }`;
}

export function PropertySelect({
  properties,
  value,
  onValueChange,
}: PropertySelectProps) {
  if (properties.length === 0) {
    return (
      <div className="flex flex-col items-start gap-3 rounded-lg border border-dashed p-4">
        <p className="text-sm text-muted-foreground">
          You haven&apos;t added any properties yet. Create one first, then come
          back to make a lease.
        </p>
        <Button variant="outline" size="sm" render={<Link href="/property" />}>
          <Plus className="size-4" />
          Create new property
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <Label htmlFor="propertyId">Property</Label>
      <Select
        name="propertyId"
        value={value}
        onValueChange={(v) => onValueChange(v ?? "")}
        items={properties.map((p) => ({
          value: p.id,
          label: propertyLabel(p),
        }))}
        required
      >
        <SelectTrigger id="propertyId" className="w-full">
          <SelectValue placeholder="Choose a property" />
        </SelectTrigger>
        <SelectContent>
          {properties.map((property) => (
            <SelectItem key={property.id} value={property.id}>
              {propertyLabel(property)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <p className="text-sm text-muted-foreground">
        Need a different one?{" "}
        <Link
          href="/landlord/property"
          className="font-medium text-primary underline"
        >
          Create new property
        </Link>
      </p>
    </div>
  );
}
