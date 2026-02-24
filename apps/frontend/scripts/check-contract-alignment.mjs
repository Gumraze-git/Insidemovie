import fs from "node:fs";
import path from "node:path";

const srcDir = path.resolve(process.cwd(), "src");

const fileExtensions = new Set([".ts", ".tsx", ".js", ".jsx"]);

const bannedPatterns = [
    {
        name: "legacy member endpoints",
        regex: /\/api\/v1\/member(?:\/|$)/g,
    },
    {
        name: "legacy match endpoints",
        regex: /\/api\/v1\/match\//g,
    },
    {
        name: "legacy recommendation endpoint",
        regex: /\/api\/v1\/recommend\/emotion/g,
    },
    {
        name: "legacy admin members endpoint",
        regex: /\/api\/v1\/admin\/members/g,
    },
    {
        name: "legacy report endpoint",
        regex: /\/api\/v1\/report\//g,
    },
    {
        name: "legacy Kakao token endpoint",
        regex: /kakao-accesstoken/g,
    },
    {
        name: "ApiResponse double-wrap parsing",
        regex: /\.data\.data/g,
    },
    {
        name: "legacy refresh header",
        regex: /Authorization-Refresh/g,
    },
    {
        name: "legacy token localStorage usage",
        regex: /localStorage\.(?:getItem|setItem|removeItem)\("(?:accessToken|refreshToken)"\)/g,
    },
    {
        name: "legacy vote average field",
        regex: /\bvoteAverage\b|\bvote_average\b/g,
    },
];

const findings = [];

const walk = (dir) => {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    for (const entry of entries) {
        const fullPath = path.join(dir, entry.name);
        if (entry.isDirectory()) {
            walk(fullPath);
            continue;
        }

        if (!entry.isFile()) {
            continue;
        }

        if (!fileExtensions.has(path.extname(entry.name))) {
            continue;
        }

        const content = fs.readFileSync(fullPath, "utf8");
        for (const pattern of bannedPatterns) {
            if (pattern.regex.test(content)) {
                findings.push({
                    file: fullPath,
                    name: pattern.name,
                });
            }
            pattern.regex.lastIndex = 0;
        }
    }
};

walk(srcDir);

if (findings.length > 0) {
    console.error("Backend contract alignment check failed:");
    for (const finding of findings) {
        console.error(`- ${finding.name}: ${finding.file}`);
    }
    process.exit(1);
}

console.log("Backend contract alignment check passed.");
