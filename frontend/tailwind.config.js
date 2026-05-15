/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: [
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "sans-serif",
        ],
        serif: ['"Source Serif 4"', "ui-serif", "Georgia", "serif"],
        mono: [
          '"JetBrains Mono"',
          "ui-monospace",
          "SFMono-Regular",
          "Menlo",
          "Consolas",
          "monospace",
        ],
      },
      colors: {
        // Academic warm-stone palette (light-mode first, like Claude)
        // ink-50  = darkest text   ink-950 = pure white page
        ink: {
          50:  "#1c1917",  // heading / strongest text
          100: "#292524",  // primary body text
          200: "#44403c",  // secondary text
          300: "#57534e",  // muted text
          400: "#78716c",  // placeholder / subtle label
          500: "#a8a29e",  // divider label / very muted
          600: "#d6d3d1",  // heavy border
          700: "#e7e5e4",  // normal border
          800: "#f5f5f4",  // card / hover background
          850: "#f9f8f7",  // panel background
          900: "#fafaf9",  // page background
          950: "#ffffff",  // pure white
        },
        // Primary interactive accent — clean blue
        accent: {
          DEFAULT: "#2563eb",  // blue-600
          soft:    "#eff6ff",  // blue-50
          subtle:  "#dbeafe",  // blue-100
          dark:    "#1d4ed8",  // blue-700
          text:    "#1e40af",  // blue-800
        },
        // Secondary accent — violet (math actors)
        violet: {
          DEFAULT: "#7c3aed",
          soft:    "#f5f3ff",
          subtle:  "#ede9fe",
          dark:    "#5b21b6",
          text:    "#4c1d95",
        },
        // Status colours — dark enough to read on white
        ok:   "#16a34a",  // green-600
        warn: "#d97706",  // amber-600
        bad:  "#dc2626",  // red-600
      },
      boxShadow: {
        panel: "0 1px 2px rgba(0,0,0,0.04), 0 4px 12px -4px rgba(0,0,0,0.08)",
        node:  "0 1px 3px rgba(0,0,0,0.06), 0 4px 10px -4px rgba(0,0,0,0.10)",
        card:  "0 0 0 1px rgba(0,0,0,0.06), 0 2px 4px rgba(0,0,0,0.04)",
      },
      keyframes: {
        pulseDot: {
          "0%, 100%": { opacity: "0.3" },
          "50%":       { opacity: "1" },
        },
      },
      animation: {
        pulseDot: "pulseDot 1.4s ease-in-out infinite",
      },
    },
  },
  plugins: [],
};
