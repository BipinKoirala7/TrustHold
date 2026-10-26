import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function Home() {
  return (
    <div className="flex flex-col flex-1 items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex flex-1 w-full max-w-3xl flex-col items-center justify-between py-32 px-16 bg-white dark:bg-black sm:items-start">
        {/* Button Variants */}
        <div className="flex flex-col gap-4 w-full">
          <p>Buttons</p>
          <div className="flex items-center gap-4 sm:items-start">
            <Button variant="default">Default</Button>
            <Button variant="link">Link</Button>
            <Button variant="secondary">Secondary</Button>
            <Button variant="destructive">Destructive</Button>
            <Button variant="ghost">Ghost</Button>
            <Button variant="outline">Outline</Button>
          </div>
        </div>

        {/* Input Fields */}
        <div className="flex flex-col gap-4 w-full">
          <p>Input Fields</p>
          <div className="flex items-center gap-4 sm:items-start">
            <Input placeholder="Default Input" />
            <Input placeholder="Disabled Input" disabled />
            <Input placeholder="Error Input" className="border-destructive" />
          </div>
        </div>

        {/* Additional UI Components */}
        <div className="flex flex-col gap-4 w-full">
          <p>Additional UI Components</p>
          <div className="flex items-center gap-4 sm:items-start">
            {/* Add more UI components here */}
          </div>
        </div>
      </main>
    </div>
  );
}
