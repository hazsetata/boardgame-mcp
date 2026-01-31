/**
 * @file App that demonstrates a few features using MCP Apps SDK with vanilla JS.
 */
import {
    App,
    applyDocumentTheme,
    applyHostFonts,
    applyHostStyleVariables,
    type McpUiHostContext,
} from "@modelcontextprotocol/ext-apps";
import type { CallToolResult } from "@modelcontextprotocol/sdk/types.js";
import "./global.css";
import "./boardgame-display.css";

function setupCoverImage(coverImageElement : HTMLElement, result: CallToolResult): void {
    const img = result.content?.find(c => c.type === 'image');
    if (img) {
        coverImageElement.innerHTML = '';

        const allowedTypes = ['image/png', 'image/jpeg', 'image/gif'];
        const mimeType = allowedTypes.includes(img.mimeType) ? img.mimeType : 'image/png';

        const image = document.createElement('img');
        image.src = `data:${mimeType};base64,${img.data}`;
        image.alt = "Board Game Cover";

        coverImageElement.appendChild(image);
    }
    else {
        coverImageElement.innerText = "[ERROR: no image in result]";
    }
}

const mainEl = document.querySelector(".main") as HTMLElement;
const coverImageEl = document.getElementById("cover-image")!;
const greetingActionEl = document.getElementById("greeting-action")!;
const greetingResponseEl = document.getElementById("greeting-response")!;
const greetingText = document.getElementById("greeting-text") as HTMLInputElement;
const sendGreetingBtn = document.getElementById("send-greeting")!;

function handleHostContextChanged(ctx: McpUiHostContext) {
    if (ctx.theme) {
        applyDocumentTheme(ctx.theme);
    }
    if (ctx.styles?.variables) {
        applyHostStyleVariables(ctx.styles.variables);
    }
    if (ctx.styles?.css?.fonts) {
        applyHostFonts(ctx.styles.css.fonts);
    }
    if (ctx.safeAreaInsets) {
        mainEl.style.paddingTop = `${ctx.safeAreaInsets.top}px`;
        mainEl.style.paddingRight = `${ctx.safeAreaInsets.right}px`;
        mainEl.style.paddingBottom = `${ctx.safeAreaInsets.bottom}px`;
        mainEl.style.paddingLeft = `${ctx.safeAreaInsets.left}px`;
    }
}

// 1. Create app instance
const app = new App({ name: "Board Game Cover Display App", version: "1.0.0" });

// 2. Register handlers BEFORE connecting
app.onteardown = async () => {
    console.info("App is being torn down");
    return {};
};

app.ontoolinput = (params) => {
    console.info("Received tool call input:", params);
};

app.ontoolresult = (result) => {
    console.info("Received tool call result:", result);
    setupCoverImage(coverImageEl, result);
};

app.ontoolcancelled = (params) => {
    console.info("Tool call cancelled:", params.reason);
};

app.onerror = console.error;

app.onhostcontextchanged = handleHostContextChanged;

sendGreetingBtn.addEventListener("click", async () => {
    console.info("Sending greeting to Host:", greetingText.value);
    await app.sendLog({ level: "info", data: greetingText.value });
    const result = await app.callServerTool({ name: "greetBoardGamers", arguments: { gamerGroup: greetingText.value } });
    console.info("greeting result:", result);

    const greetingMessage = result.structuredContent?.greetingMessage as string | undefined;

    if (greetingMessage) {
        const greetingHeading = document.createElement('h3')
        greetingHeading.innerText = greetingMessage;
        greetingResponseEl.appendChild(greetingHeading);

        greetingActionEl.style.display = 'none';
    }
});

// 3. Connect to host
app.connect().then(() => {
    const ctx = app.getHostContext();
    if (ctx) {
        handleHostContextChanged(ctx);
    }
});