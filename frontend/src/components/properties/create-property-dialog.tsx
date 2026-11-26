"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  CreatePropertyDto,
  createPropertySchema,
  propertyTypeLabels,
} from "@/lib/schema/propertySchema";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Spinner } from "@/components/ui/spinner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCreateProperty } from "@/hooks/useCreateProperty";

function CreatePropertyDialog() {
  const [open, setOpen] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreatePropertyDto>({
    resolver: zodResolver(createPropertySchema),
    defaultValues: {
      name: "",
      type: "APARTMENT",
      addressLine: "",
      city: "",
      description: "",
    },
  });

  const { mutate, isPending } = useCreateProperty(() => {
    setOpen(false);
    reset();
  });

  function handleOpenChange(next: boolean) {
    if (isPending) return;
    setOpen(next);
    if (!next) reset();
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger
        render={
          <Button>
            <Plus /> <span>New Property</span>
          </Button>
        }
      ></DialogTrigger>

      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className={"text-lg font-semibold"}>
            New Property
          </DialogTitle>
          <DialogDescription>
            Add a property you can attach to a lease.
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={(e) => {
            e.stopPropagation();
            handleSubmit((values) => mutate(values))(e);
          }}
          className="space-y-4"
        >
          <div className="space-y-2">
            <Label htmlFor="name">Property name</Label>
            <Input
              id="name"
              placeholder="e.g. Lakeside Apartment"
              aria-invalid={!!errors.name}
              {...register("name")}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="type">Property type</Label>
            <Controller
              control={control}
              name="type"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger id="type" className="w-full">
                    <SelectValue>{propertyTypeLabels[field.value]}</SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="APARTMENT">Apartment</SelectItem>
                    <SelectItem value="HOUSE">House</SelectItem>
                    <SelectItem value="LAND">Land</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="addressLine">Address</Label>
              <Input
                id="addressLine"
                placeholder="Street, ward, landmark"
                aria-invalid={!!errors.addressLine}
                {...register("addressLine")}
              />
              {errors.addressLine && (
                <p className="text-sm text-destructive">
                  {errors.addressLine.message}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="city">City</Label>
              <Input
                id="city"
                placeholder="e.g. Pokhara"
                aria-invalid={!!errors.city}
                {...register("city")}
              />
              {errors.city && (
                <p className="text-sm text-destructive">
                  {errors.city.message}
                </p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              rows={4}
              placeholder="Floors, rooms, access, anything the tenant should know"
              aria-invalid={!!errors.description}
              {...register("description")}
            />
            {errors.description && (
              <p className="text-sm text-destructive">
                {errors.description.message}
              </p>
            )}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              disabled={isPending}
              onClick={() => handleOpenChange(false)}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Spinner />}
              Create property
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default CreatePropertyDialog;
