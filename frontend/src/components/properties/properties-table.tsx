"use client";

import { Building2, SearchX, TriangleAlert } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/lib/query-cache-key";
import {
  getLandlordProperties,
  Property,
  PropertyType,
} from "@/lib/api/property";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const headClass =
  "text-xs font-semibold uppercase tracking-wide text-muted-foreground";

interface PropertiesTableProps {
  search: string;
  type: PropertyType | "ALL";
}

function PropertiesTable({ search, type }: PropertiesTableProps) {
  const { isPending, data, isError } = useQuery<Property[]>({
    queryKey: queryKeys.properties,
    queryFn: getLandlordProperties,
  });

  const query = search.trim().toLowerCase();
  const filtered = (data ?? []).filter((property) => {
    const matchesType = type === "ALL" || property.type === type;
    const matchesSearch =
      property.name.toLowerCase().includes(query) ||
      property.addressLine.toLowerCase().includes(query) ||
      property.city.toLowerCase().includes(query);
    return matchesType && matchesSearch;
  });

  const hasNoProperties = !isPending && !isError && data?.length === 0;
  const hasNoMatches =
    !isPending && !isError && data?.length !== 0 && filtered.length === 0;

  return (
    <div className="rounded-xl border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className={`${headClass} pl-6`}>Name</TableHead>
            <TableHead className={headClass}>Type</TableHead>
            <TableHead className={headClass}>Location</TableHead>
            <TableHead className={`${headClass} pr-6`}>Updated</TableHead>
          </TableRow>
        </TableHeader>

        <TableBody>
          {isPending &&
            Array.from({ length: 5 }).map((_, i) => (
              <TableRow key={i}>
                <TableCell className="pl-6">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="mt-2 h-3 w-56" />
                </TableCell>
                <TableCell>
                  <Skeleton className="h-5 w-20 rounded-full" />
                </TableCell>
                <TableCell>
                  <Skeleton className="h-4 w-44" />
                </TableCell>
                <TableCell className="pr-6">
                  <Skeleton className="h-4 w-24" />
                </TableCell>
              </TableRow>
            ))}

          {isError && (
            <TableRow className="hover:bg-transparent">
              <TableCell colSpan={4} className="py-16">
                <div className="flex flex-col items-center justify-center gap-2 text-center">
                  <div className="flex size-12 items-center justify-center rounded-full bg-destructive/10">
                    <TriangleAlert className="size-6 text-destructive" />
                  </div>
                  <p className="font-medium">Something went wrong</p>
                  <p className="text-sm text-muted-foreground">
                    We couldn&apos;t load your properties. Please try again.
                  </p>
                </div>
              </TableCell>
            </TableRow>
          )}

          {hasNoProperties && (
            <TableRow className="hover:bg-transparent">
              <TableCell colSpan={4} className="py-16">
                <div className="flex flex-col items-center justify-center gap-2 text-center">
                  <div className="flex size-12 items-center justify-center rounded-full bg-muted">
                    <Building2 className="size-6 text-muted-foreground" />
                  </div>
                  <p className="font-medium">No properties yet</p>
                  <p className="text-sm text-muted-foreground">
                    Properties you add while creating a lease will appear here.
                  </p>
                </div>
              </TableCell>
            </TableRow>
          )}

          {hasNoMatches && (
            <TableRow className="hover:bg-transparent">
              <TableCell colSpan={4} className="py-16">
                <div className="flex flex-col items-center justify-center gap-2 text-center">
                  <div className="flex size-12 items-center justify-center rounded-full bg-muted">
                    <SearchX className="size-6 text-muted-foreground" />
                  </div>
                  <p className="font-medium">No matching properties</p>
                  <p className="text-sm text-muted-foreground">
                    Try a different search or property type.
                  </p>
                </div>
              </TableCell>
            </TableRow>
          )}

          {filtered.map((property) => (
            <TableRow key={property.id}>
              <TableCell className="pl-6">
                <div className="font-medium">{property.name}</div>
                <div className="max-w-xs truncate text-sm text-muted-foreground">
                  {property.description}
                </div>
              </TableCell>
              <TableCell>
                <Badge variant="secondary">
                  {property.type.charAt(0) +
                    property.type.slice(1).toLowerCase()}
                </Badge>
              </TableCell>
              <TableCell className="text-muted-foreground">
                {property.addressLine}, {property.city}
              </TableCell>
              <TableCell className="pr-6 text-muted-foreground">
                {new Date(property.updatedAt).toLocaleDateString("en-US", {
                  month: "short",
                  day: "numeric",
                  year: "numeric",
                })}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

export default PropertiesTable;
