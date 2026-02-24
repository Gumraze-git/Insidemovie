export type DemoAccountCategory = "ONBOARDING" | "GENERAL";

export interface DemoAccountOption {
    accountKey: string;
    label: string;
    category: DemoAccountCategory;
}

export interface DemoAccountsApiResponse {
    accounts: DemoAccountOption[];
}
