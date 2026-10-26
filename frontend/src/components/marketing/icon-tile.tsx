import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const iconTileVariants = cva("flex shrink-0 items-center justify-center", {
  variants: {
    tone: {
      mint: "bg-primary/10 text-primary",
      solid: "bg-primary text-primary-foreground",
      amber: "bg-amber-100 text-amber-800",
    },
    size: {
      sm: "size-9 [&_svg]:size-4",
      md: "size-12 [&_svg]:size-5",
      lg: "size-14 [&_svg]:size-6",
    },
    shape: {
      square: "rounded-xl",
      circle: "rounded-full",
    },
  },
  defaultVariants: {
    tone: "mint",
    size: "md",
    shape: "square",
  },
});

type IconTileProps = React.ComponentProps<"span"> &
  VariantProps<typeof iconTileVariants>;

export function IconTile({
  tone,
  size,
  shape,
  className,
  ...props
}: IconTileProps) {
  return (
    <span
      aria-hidden
      className={cn(iconTileVariants({ tone, size, shape }), className)}
      {...props}
    />
  );
}
