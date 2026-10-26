import { Button } from "../ui/button";
import { Input } from "../ui/input";

const inputClass =
  "h-12 rounded-xl bg-card px-4 text-base md:text-base border-border";

export function ResetPasswordForm() {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <label
          htmlFor="password"
          className="text-sm font-medium text-foreground"
        >
          Email
        </label>
        <Input
          id="email"
          placeholder="your.email@example.com"
          className={inputClass}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Button>Send Reset Link</Button>
        <Button variant="outline" className="w-full">
          Back to Login
        </Button>
      </div>
    </div>
  );
}
