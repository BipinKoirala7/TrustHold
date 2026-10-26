import type { Metadata } from "next";
import {
  Banknote,
  Building2,
  Camera,
  Handshake,
  KeyRound,
  User,
} from "lucide-react";
import { MarketingNavbar } from "@/components/marketing/marketing-navbar";
import { Hero } from "@/components/marketing/hero";
import { SectionHeading } from "@/components/marketing/section-heading";
import { StepCard } from "@/components/marketing/step-card";
import { AudienceCard } from "@/components/marketing/audience-card";
import { CtaBanner } from "@/components/marketing/cta-banner";
import { MarketingFooter } from "@/components/marketing/marketing-footer";

export const metadata: Metadata = {
  title: "TrustHold — Rental deposit escrow for Nepal",
  description:
    "TrustHold holds the security deposit in escrow until landlord and tenant both agree on the settlement.",
};

const steps = [
  {
    number: "01",
    icon: KeyRound,
    title: "Create a private lease",
    description:
      "The landlord records the property, tenant and terms in one secure place.",
  },
  {
    number: "02",
    icon: Banknote,
    title: "Pay into escrow",
    description:
      "The tenant pays the security deposit via eSewa. It is held safely until the lease closes.",
  },
  {
    number: "03",
    icon: Camera,
    title: "Document together",
    description:
      "Both parties add labelled move-in and move-out photos for a shared record.",
  },
  {
    number: "04",
    icon: Handshake,
    title: "Agree and release",
    description:
      "You agree on the split; an admin confirms and releases the funds.",
  },
];

const landlordItems = [
  "Create a private lease in minutes",
  "Deposit is secured before move-in",
  "See when deposits and photos are complete",
  "Propose a clear, itemised settlement",
];

const tenantItems = [
  "Your money is never in the landlord's pocket",
  "Move-in photos protect you at move-out",
  "Counter-propose if you disagree",
  "Agree to every settlement before release",
];

export default function HomePage() {
  return (
    <>
      <MarketingNavbar />

      <main>
        <Hero />

        <section
          id="how-it-works"
          className="scroll-mt-16 bg-card py-20 md:py-28"
        >
          <div className="mx-auto max-w-6xl px-6">
            <SectionHeading
              eyebrow="How it works"
              title="One clear path from move-in to release"
              description="No public listings. No automated judgement. Just one private lease shared by the people involved."
            />
            <ol className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {steps.map((step) => (
                <StepCard key={step.number} {...step} />
              ))}
            </ol>
          </div>
        </section>

        <section className="bg-background py-20 md:py-28">
          <div className="mx-auto max-w-6xl px-6">
            <div className="grid gap-6 md:grid-cols-2">
              <AudienceCard
                icon={Building2}
                eyebrow="For landlords"
                title="A professional process, without the friction"
                items={landlordItems}
              />
              <AudienceCard
                icon={User}
                eyebrow="For tenants"
                title="Your deposit and records stay visible"
                items={tenantItems}
              />
            </div>

            <div className="mt-16">
              <CtaBanner />
            </div>
          </div>
        </section>
      </main>

      <MarketingFooter />
    </>
  );
}
