"use client";

import { useState } from "react";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { useDeleteProperty } from "@/hooks/use-delete-property";

interface DeletePropertyButtonProps {
  id: string;
  name: string;
}

function DeletePropertyButton({ id, name }: DeletePropertyButtonProps) {
  const [open, setOpen] = useState(false);
  const { mutate, isPending } = useDeleteProperty(() => setOpen(false));

  return (
    <>
      <Button
        variant="ghost"
        size="icon"
        aria-label={`Delete ${name}`}
        onClick={() => setOpen(true)}
      >
        <Trash2 className="size-4" />
      </Button>

      <AlertDialog
        open={open}
        onOpenChange={(next) => !isPending && setOpen(next)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete property?</AlertDialogTitle>
            <AlertDialogDescription>
              &quot;{name}&quot; will be permanently removed. This can&apos;t be
              undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isPending}>Cancel</AlertDialogCancel>
            <Button
              variant="destructive"
              disabled={isPending}
              onClick={() => mutate(id)}
            >
              {isPending && <Spinner />}
              Delete
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

export default DeletePropertyButton;
