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
const ratingText = document.getElementById("rating-text") as HTMLInputElement;
const sendRatingBtn = document.getElementById("send-rating")!;

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

sendRatingBtn.addEventListener("click", async () => {
    console.info("Sending rating to Host:", ratingText.value);
    await app.sendLog({ level: "info", data: ratingText.value });
});

// 3. Connect to host
app.connect().then(() => {
    const ctx = app.getHostContext();
    if (ctx) {
        handleHostContextChanged(ctx);
    }
});