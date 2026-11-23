"use client";

import { useState } from "react";
import { Filter, Search } from "lucide-react";
import { PropertyType, propertyTypeLabels } from "@/lib/schema/propertySchema";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import PropertiesTable from "@/components/properties/properties-table";
import CreatePropertyDialog from "@/components/properties/create-property-dialog";

function PropertiesPage() {
  const [search, setSearch] = useState("");
  const [type, setType] = useState<PropertyType | "ALL">("ALL");

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-start justify-between gap-2 sm:flex-row sm:items-center">
        <div className="flex flex-col">
          <h1 className="text-2xl font-bold tracking-tight">Properties</h1>
          <p className="text-muted-foreground">
            View the properties linked to your leases.
          </p>
        </div>
        <CreatePropertyDialog />
      </div>

      <div className="flex flex-col gap-3 rounded-xl border bg-card p-4 shadow-sm sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name, address or city..."
            className="pl-9"
          />
        </div>

        <Select
          value={type}
          onValueChange={(value) => setType(value as PropertyType | "ALL")}
        >
          <SelectTrigger className="sm:w-48">
            <Filter className="size-4" />
            <SelectValue>
              {type === "ALL" ? "All" : propertyTypeLabels[type]}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All</SelectItem>
            <SelectItem value="APARTMENT">Apartment</SelectItem>
            <SelectItem value="HOUSE">House</SelectItem>
            <SelectItem value="LAND">Land</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <PropertiesTable search={search} type={type} />
    </div>
  );
}

export default PropertiesPage;
