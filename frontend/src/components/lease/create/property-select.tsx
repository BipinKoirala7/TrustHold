"use client";

import { AlertCircle, MapPin, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  PropertyDetailsDto,
  propertyTypeLabels,
} from "@/lib/schema/propertySchema";
import CreatePropertyDialog from "@/components/properties/create-property-dialog";

type PropertySelectProps = {
  value: string;
  onValueChange: (id: string) => void;
  error?: string;
  properties: PropertyDetailsDto[];
  isLoading: boolean;
  isError: boolean;
  isFetching: boolean;
  refetch: () => void;
};

function propertyOption(property: PropertyDetailsDto) {
  const badge = property.isArchived
    ? "Archived"
    : property.isPropertyLeased
      ? "In another lease"
      : null;

  return (
    <div className="flex w-full min-w-0 items-center justify-between gap-3 py-1">
      <div className="flex min-w-0 flex-col gap-0.5">
        <p className="truncate text-sm font-medium">{property.name}</p>
        <p className="truncate text-xs text-muted-foreground">
          {propertyTypeLabels[property.type]} · {property.addressLine},{" "}
          {property.city}
        </p>
      </div>

      {badge && (
        <span className="shrink-0 rounded-full border bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground">
          {badge}
        </span>
      )}
    </div>
  );
}

function propertyCard(property: PropertyDetailsDto) {
  return (
    <div className="space-y-3 rounded-lg border bg-card p-4">
      <div className="flex items-start justify-between gap-3">
        <p className="min-w-0 truncate text-sm font-semibold">
          {property.name}
        </p>
        <span className="shrink-0 rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
          {propertyTypeLabels[property.type]}
        </span>
      </div>

      <div className="flex items-start gap-1.5 text-sm text-muted-foreground">
        <MapPin className="mt-0.5 size-4 shrink-0" />
        <p>
          {property.addressLine}, {property.city}
        </p>
      </div>

      {property.description && (
        <p className="line-clamp-2 border-t pt-3 text-sm text-muted-foreground">
          {property.description}
        </p>
      )}
    </div>
  );
}

export function PropertySelect({
  value,
  onValueChange,
  error,
  properties,
  isLoading,
  isError,
  isFetching,
  refetch,
}: PropertySelectProps) {
  if (isLoading) {
    return (
      <div className="space-y-2">
        <Skeleton className="h-4 w-16" />
        <Skeleton className="h-9 w-full" />
        <Skeleton className="h-4 w-48" />
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex flex-col items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4">
        <div className="flex items-start gap-2">
          <AlertCircle className="mt-0.5 size-4 shrink-0 text-destructive" />
          <p className="text-sm text-muted-foreground">
            We couldn&apos;t load your properties. Check your connection and try
            again.
          </p>
        </div>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          disabled={isFetching}
        >
          {isFetching ? "Retrying..." : "Try again"}
        </Button>
      </div>
    );
  }

  if (properties.length === 0) {
    return (
      <div className="flex flex-col items-start gap-3 rounded-lg border border-dashed p-4">
        <p className="text-sm text-muted-foreground">
          You haven&apos;t added any properties yet. Create one first, then come
          back to make a lease.
        </p>
        <CreatePropertyDialog />
      </div>
    );
  }

  const selected = properties.find((p) => p.id === value);

  return (
    <div className="space-y-2">
      <Label htmlFor="propertyId">Property</Label>

      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <div className="min-w-0 flex-1">
          <Select
            value={value}
            onValueChange={(v) => onValueChange(v ?? "")}
            items={properties.map((p) => ({ value: p.id, label: p.name }))}
          >
            <SelectTrigger
              id="propertyId"
              className="w-full"
              aria-invalid={!!error}
            >
              <SelectValue placeholder="Choose a property" />
            </SelectTrigger>
            <SelectContent alignItemWithTrigger={false}>
              {properties.map((property) => (
                <SelectItem
                  key={property.id}
                  value={property.id}
                  disabled={property.isArchived || property.isPropertyLeased}
                >
                  {propertyOption(property)}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <CreatePropertyDialog />
      </div>

      {error && <p className="text-sm text-destructive">{error}</p>}

      {selected && propertyCard(selected)}
    </div>
  );
}
