document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".board_write_form");

    const editor = document.querySelector("[data-editor]");
    const content = editor?.querySelector("[data-editor-content]");
    const contentInput = document.getElementById("contentInput");

    const commandMenu = editor?.querySelector("[data-command-menu]");
    const commandItems = editor?.querySelectorAll(".wt_editor_command_item");

    const inlineToolbar = editor?.querySelector("[data-inline-toolbar]");
    const inlineButtons = editor?.querySelectorAll(".wt_editor_inline_btn");

    if (
        !form ||
        !editor ||
        !content ||
        !contentInput ||
        !commandMenu ||
        !commandItems.length ||
        !inlineToolbar ||
        !inlineButtons.length
    ) {
        return;
    }

    let selectedIndex = 0;
    let savedRange = null;

    /* ==========================================================
       Form Submit
    ========================================================== */

    form.addEventListener("submit", function (event) {
        const plainText = content.innerText.trim();

        if (!plainText) {
            event.preventDefault();
            alert("내용을 입력하세요.");
            content.focus();
            return;
        }

        contentInput.value = content.innerHTML;
    });

    /* ==========================================================
       Selection
    ========================================================== */

    function saveSelection() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) {
            return;
        }

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) {
            return;
        }

        savedRange = range.cloneRange();
    }

    function restoreSelection() {
        if (!savedRange) {
            content.focus();
            return;
        }

        content.focus();

        const selection = window.getSelection();
        selection.removeAllRanges();
        selection.addRange(savedRange);
    }

    content.addEventListener("keyup", saveSelection);
    content.addEventListener("mouseup", saveSelection);
    content.addEventListener("input", saveSelection);

    /* ==========================================================
       Slash Command
    ========================================================== */

    content.addEventListener("input", function () {
        saveSelection();

        const text = content.innerText.trimEnd();

        if (text.endsWith("/")) {
            openCommandMenu();
            return;
        }

        closeCommandMenu();
    });

    content.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeCommandMenu();
            closeInlineToolbar();
            return;
        }

        if (!commandMenu.classList.contains("is_active")) {
            return;
        }

        if (event.key === "ArrowDown") {
            event.preventDefault();

            selectedIndex = (selectedIndex + 1) % commandItems.length;
            updateSelectedItem();
        }

        if (event.key === "ArrowUp") {
            event.preventDefault();

            selectedIndex =
                (selectedIndex - 1 + commandItems.length) % commandItems.length;
            updateSelectedItem();
        }

        if (event.key === "Enter") {
            event.preventDefault();

            const selectedItem = commandItems[selectedIndex];

            if (selectedItem) {
                applyCommand(selectedItem.dataset.command);
            }
        }
    });

    commandItems.forEach(function (item, index) {
        item.addEventListener("mouseenter", function () {
            selectedIndex = index;
            updateSelectedItem();
        });

        item.addEventListener("mousedown", function (event) {
            event.preventDefault();
        });

        item.addEventListener("click", function () {
            applyCommand(item.dataset.command);
        });
    });

    function openCommandMenu() {
        selectedIndex = 0;

        saveSelection();
        closeInlineToolbar();

        commandMenu.classList.add("is_active");
        updateSelectedItem();

        const rect = getCurrentRangeRect();
        const editorRect = editor.getBoundingClientRect();

        if (!rect) {
            commandMenu.style.left = "16px";
            commandMenu.style.top = "48px";
            return;
        }

        commandMenu.style.left = `${rect.left - editorRect.left}px`;
        commandMenu.style.top = `${rect.bottom - editorRect.top + 8}px`;
    }

    function closeCommandMenu() {
        commandMenu.classList.remove("is_active");
    }

    function updateSelectedItem() {
        commandItems.forEach(function (item, index) {
            item.classList.toggle("is_selected", index === selectedIndex);
        });
    }

    function applyCommand(command) {
        restoreSelection();
        removeLastSlash();

        const node = createCommandNode(command);

        if (!node) {
            closeCommandMenu();
            return;
        }

        insertNodeAtCursor(node);

        const paragraph = document.createElement("p");
        paragraph.innerHTML = "<br>";
        insertNodeAtCursor(paragraph);

        closeCommandMenu();
        saveSelection();
    }

    function createCommandNode(command) {
        if (command === "h1") {
            const h2 = document.createElement("h2");
            h2.textContent = "제목 1";
            return h2;
        }

        if (command === "h2") {
            const h3 = document.createElement("h3");
            h3.textContent = "제목 2";
            return h3;
        }

        if (command === "bullet") {
            const ul = document.createElement("ul");
            ul.innerHTML = "<li>목록</li>";
            return ul;
        }

        if (command === "number") {
            const ol = document.createElement("ol");
            ol.innerHTML = "<li>목록</li>";
            return ol;
        }

        if (command === "check") {
            const div = document.createElement("div");
            div.className = "wt_editor_check_item";
            div.innerHTML = '<input type="checkbox"> <span>할 일</span>';
            return div;
        }

        if (command === "quote") {
            const quote = document.createElement("blockquote");
            quote.textContent = "인용문";
            return quote;
        }

        if (command === "code") {
            const pre = document.createElement("pre");
            const code = document.createElement("code");

            code.textContent = "코드를 입력하세요";
            pre.appendChild(code);

            return pre;
        }

        if (command === "line") {
            return document.createElement("hr");
        }

        return null;
    }

    /* ==========================================================
       Inline Toolbar
    ========================================================== */

    content.addEventListener("mouseup", updateInlineToolbar);
    content.addEventListener("keyup", updateInlineToolbar);

    document.addEventListener("selectionchange", function () {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
            closeInlineToolbar();
            return;
        }

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) {
            closeInlineToolbar();
        }
    });

    content.addEventListener("blur", function () {
        setTimeout(function () {
            const activeElement = document.activeElement;

            if (
                inlineToolbar.contains(activeElement) ||
                commandMenu.contains(activeElement)
            ) {
                return;
            }

            closeInlineToolbar();
        }, 0);
    });

    inlineButtons.forEach(function (button) {
        button.addEventListener("mousedown", function (event) {
            event.preventDefault();
        });

        button.addEventListener("click", function () {
            applyInline(button.dataset.inline);
        });
    });

    function updateInlineToolbar() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
            closeInlineToolbar();
            return;
        }

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) {
            closeInlineToolbar();
            return;
        }

        saveSelection();
        closeCommandMenu();

        const rect = range.getBoundingClientRect();
        const editorRect = editor.getBoundingClientRect();

        inlineToolbar.classList.add("is_active");

        inlineToolbar.style.left =
            `${rect.left - editorRect.left + rect.width / 2}px`;

        inlineToolbar.style.top =
            `${rect.top - editorRect.top - 48}px`;
    }

    function closeInlineToolbar() {
        inlineToolbar.classList.remove("is_active");
    }

    function applyInline(type) {
        restoreSelection();

        if (type === "bold") {
            wrapSelection("strong");
        }

        if (type === "italic") {
            wrapSelection("em");
        }

        if (type === "link") {
            const url = prompt("링크 주소를 입력하세요", "https://");

            if (!url || url === "https://") {
                return;
            }

            wrapSelection("a", {
                href: url,
                target: "_blank",
                rel: "noopener noreferrer"
            });
        }

        closeInlineToolbar();
        saveSelection();
    }

    function wrapSelection(tagName, attributes = {}) {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
            return;
        }

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) {
            return;
        }

        const wrapper = document.createElement(tagName);

        Object.keys(attributes).forEach(function (key) {
            wrapper.setAttribute(key, attributes[key]);
        });

        try {
            range.surroundContents(wrapper);
        } catch (error) {
            const selectedContent = range.extractContents();
            wrapper.appendChild(selectedContent);
            range.insertNode(wrapper);
        }

        const newRange = document.createRange();
        newRange.selectNodeContents(wrapper);

        selection.removeAllRanges();
        selection.addRange(newRange);

        savedRange = newRange.cloneRange();
    }

    /* ==========================================================
       Helpers
    ========================================================== */

    function getTextBeforeCursor() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) {
            return "";
        }

        const range = selection.getRangeAt(0);
        const beforeRange = range.cloneRange();

        beforeRange.selectNodeContents(content);
        beforeRange.setEnd(range.endContainer, range.endOffset);

        return beforeRange.toString();
    }

    function removeLastSlash() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) {
            return;
        }

        const range = selection.getRangeAt(0);

        if (
            range.startContainer.nodeType === Node.TEXT_NODE &&
            range.startOffset > 0
        ) {
            const textNode = range.startContainer;
            const text = textNode.textContent;
            const slashIndex = range.startOffset - 1;

            if (text.charAt(slashIndex) === "/") {
                textNode.textContent =
                    text.substring(0, slashIndex) +
                    text.substring(range.startOffset);

                const newRange = document.createRange();
                newRange.setStart(textNode, slashIndex);
                newRange.collapse(true);

                selection.removeAllRanges();
                selection.addRange(newRange);

                savedRange = newRange.cloneRange();
            }
        }
    }

    function insertNodeAtCursor(node) {
        restoreSelection();

        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) {
            content.appendChild(node);
            moveCursorAfter(node);
            return;
        }

        const range = selection.getRangeAt(0);

        range.deleteContents();
        range.insertNode(node);

        moveCursorAfter(node);
    }

    function moveCursorAfter(node) {
        const range = document.createRange();
        const selection = window.getSelection();

        range.setStartAfter(node);
        range.collapse(true);

        selection.removeAllRanges();
        selection.addRange(range);

        savedRange = range.cloneRange();
    }

    function getCurrentRangeRect() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) {
            return null;
        }

        const range = selection.getRangeAt(0).cloneRange();
        let rect = range.getBoundingClientRect();

        if (rect.width === 0 && rect.height === 0) {
            const marker = document.createElement("span");
            marker.textContent = "\u200b";

            range.insertNode(marker);
            rect = marker.getBoundingClientRect();

            marker.parentNode.removeChild(marker);

            const newRange = document.createRange();
            newRange.setStart(range.startContainer, range.startOffset);
            newRange.collapse(true);

            const selection = window.getSelection();
            selection.removeAllRanges();
            selection.addRange(newRange);

            savedRange = newRange.cloneRange();
        }

        return rect;
    }

    /* ==========================================================
       Outside Click
    ========================================================== */

    document.addEventListener("click", function (event) {
        if (!editor.contains(event.target)) {
            closeCommandMenu();
            closeInlineToolbar();
        }
    });
});