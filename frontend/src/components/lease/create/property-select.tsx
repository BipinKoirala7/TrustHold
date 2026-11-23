"use client";

import Link from "next/link";
import { AlertCircle, Plus } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
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
import { queryKeys } from "@/lib/query-cache-key";
import { getLandlordProperties } from "@/lib/api/property";
import { Property } from "@/lib/schema/propertySchema";
import CreatePropertyDialog from "@/components/properties/create-property-dialog";

type PropertySelectProps = {
  value: string;
  onValueChange: (id: string) => void;
  error?: string;
};

function propertyLabel(property: Property) {
  return `${property.addressLine}, ${property.city}${
    property.description ? ` · ${property.type}` : ""
  }`;
}

export function PropertySelect({
  value,
  onValueChange,
  error,
}: PropertySelectProps) {
  const { data, isLoading, isError, refetch, isFetching } = useQuery({
    queryKey: queryKeys.properties,
    queryFn: getLandlordProperties,
  });

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

  const properties = data ?? [];

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

  return (
    <div className="space-y-2">
      <Label htmlFor="propertyId">Property</Label>
      <Select
        value={value}
        onValueChange={(v) => onValueChange(v ?? "")}
        items={properties.map((p) => ({
          value: p.id,
          label: propertyLabel(p),
        }))}
      >
        <SelectTrigger
          id="propertyId"
          className="w-full"
          aria-invalid={!!error}
        >
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
      {error && <p className="text-sm text-destructive">{error}</p>}
      <p className="text-sm text-muted-foreground">
        Need a different one? <CreatePropertyDialog />
      </p>
    </div>
  );
}
