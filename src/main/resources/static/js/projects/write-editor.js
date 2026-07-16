document.addEventListener("DOMContentLoaded", function () {
    const editor = document.querySelector("[data-editor]");
    if (!editor) return;

    const content = editor.querySelector("[data-editor-content]");
    const commandMenu = editor.querySelector("[data-command-menu]");
    const commandItems = editor.querySelectorAll(".wt_editor_command_item");

    const inlineToolbar = editor.querySelector("[data-inline-toolbar]");
    const inlineButtons = editor.querySelectorAll(".wt_editor_inline_btn");

    const colorPalette = editor.querySelector("[data-color-palette]");
    const colorButtons = editor.querySelectorAll("[data-color-palette] button");

    const highlightPalette = editor.querySelector("[data-highlight-palette]");
    const highlightButtons = editor.querySelectorAll("[data-highlight-palette] button");

    if (
        !content ||
        !commandMenu ||
        !commandItems.length ||
        !inlineToolbar ||
        !inlineButtons.length ||
        !colorPalette ||
        !colorButtons.length ||
        !highlightPalette ||
        !highlightButtons.length
    ) {
        return;
    }

    let selectedIndex = 0;
    let savedRange = null;

    /* ==========================================================
       Selection
    ========================================================== */

    function saveSelection() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) return;

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) return;

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
       Popup
    ========================================================== */

    function closeToolbarPopups() {
        colorPalette.classList.remove("is_active");
        highlightPalette.classList.remove("is_active");
    }

    function closeInlineToolbar() {
        inlineToolbar.classList.remove("is_active");
        closeToolbarPopups();
    }

    function closeAllPopups() {
        commandMenu.classList.remove("is_active");
        closeInlineToolbar();
    }

    function openToolbarPopup(popup) {
        restoreSelection();
        closeToolbarPopups();

        popup.classList.add("is_active");

        const toolbarRect = inlineToolbar.getBoundingClientRect();
        const editorRect = editor.getBoundingClientRect();

        popup.style.left = `${toolbarRect.left - editorRect.left}px`;
        popup.style.top = `${toolbarRect.bottom - editorRect.top + 8}px`;
    }

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

        commandMenu.classList.remove("is_active");
    });

    content.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeAllPopups();
            return;
        }

        if (!commandMenu.classList.contains("is_active")) return;

        if (event.key === "ArrowDown") {
            event.preventDefault();
            selectedIndex = (selectedIndex + 1) % commandItems.length;
            updateSelectedItem();
        }

        if (event.key === "ArrowUp") {
            event.preventDefault();
            selectedIndex = (selectedIndex - 1 + commandItems.length) % commandItems.length;
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
            commandMenu.classList.remove("is_active");
            return;
        }

        insertNodeAtCursor(node);

        const paragraph = document.createElement("p");
        paragraph.innerHTML = "<br>";
        insertNodeAtCursor(paragraph);

        commandMenu.classList.remove("is_active");
        saveSelection();
    }

    const commandFactory = {
        h1() {
            const h2 = document.createElement("h2");
            h2.textContent = "제목 1";
            return h2;
        },

        h2() {
            const h3 = document.createElement("h3");
            h3.textContent = "제목 2";
            return h3;
        },

        bullet() {
            const ul = document.createElement("ul");
            ul.innerHTML = "<li>목록</li>";
            return ul;
        },

        number() {
            const ol = document.createElement("ol");
            ol.innerHTML = "<li>목록</li>";
            return ol;
        },

        check() {
            const div = document.createElement("div");
            div.className = "wt_editor_check_item";
            div.innerHTML = '<input type="checkbox"> <span>할 일</span>';
            return div;
        },

        quote() {
            const quote = document.createElement("blockquote");
            quote.textContent = "인용문";
            return quote;
        },

        code() {
            const pre = document.createElement("pre");
            const code = document.createElement("code");

            code.textContent = "코드를 입력하세요";
            pre.appendChild(code);

            return pre;
        },

        line() {
            return document.createElement("hr");
        }
    };

    function createCommandNode(command) {
        return commandFactory[command]?.() ?? null;
    }

    /* ==========================================================
       Inline Toolbar
    ========================================================== */

    content.addEventListener("mouseup", function () {
        setTimeout(updateInlineToolbar, 0);
    });

    content.addEventListener("keyup", updateInlineToolbar);

    document.addEventListener("selectionchange", function () {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
            closeInlineToolbar();
            return;
        }

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) {
            closeAllPopups();
            return;
        }

        setTimeout(updateInlineToolbar, 0);
    });

    content.addEventListener("blur", function () {
        setTimeout(function () {
            const activeElement = document.activeElement;

            if (
                inlineToolbar.contains(activeElement) ||
                commandMenu.contains(activeElement) ||
                colorPalette.contains(activeElement) ||
                highlightPalette.contains(activeElement)
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
            closeAllPopups();
            return;
        }

        saveSelection();

        commandMenu.classList.remove("is_active");

        const rect = range.getBoundingClientRect();
        const editorRect = editor.getBoundingClientRect();

        inlineToolbar.classList.add("is_active");

        inlineToolbar.style.left = `${rect.left - editorRect.left + rect.width / 2}px`;
        inlineToolbar.style.top = `${rect.top - editorRect.top - 48}px`;
    }

    function applyInline(type) {
        restoreSelection();

        const inlineActions = {
            bold() {
                toggleInline("strong");
            },

            italic() {
                toggleInline("em");
            },

            strike() {
                toggleInline("s");
            },

            link() {
                const url = prompt("링크 주소를 입력하세요", "https://");

                if (!url || url === "https://") return;

                wrapSelection("a", {
                    href: url,
                    target: "_blank",
                    rel: "noopener noreferrer"
                });
            },

            color() {
                openToolbarPopup(colorPalette);
            },

            highlight() {
                openToolbarPopup(highlightPalette);
            }
        };

        inlineActions[type]?.();

        if (type === "color" || type === "highlight") return;

        closeAllPopups();
        saveSelection();
    }

    /* ==========================================================
       Color / Highlight
    ========================================================== */

    colorButtons.forEach(function (button) {
        const color = button.dataset.color;
        button.style.background = color;

        button.addEventListener("mousedown", function (event) {
            event.preventDefault();
        });

        button.addEventListener("click", function () {
            applyStyleToSelection("color", color);
            closeAllPopups();
            saveSelection();
        });
    });

    highlightButtons.forEach(function (button) {
        const color = button.dataset.highlight;
        button.style.background = color;

        button.addEventListener("mousedown", function (event) {
            event.preventDefault();
        });

        button.addEventListener("click", function () {
            applyStyleToSelection("backgroundColor", color);
            closeAllPopups();
            saveSelection();
        });
    });

    /* ==========================================================
       Text Wrapper
    ========================================================== */

    /**
     * 인라인 스타일(굵게, 기울기, 취소선) 토글
     * - 이미 같은 태그 안이면 스타일 제거
     * - 없으면 새로 적용
     */
    function toggleInline(tagName) {
        restoreSelection();

        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
            return;
        }

        const range = selection.getRangeAt(0);

        // 1. 선택 시작 지점 기준으로 부모 태그 검사
        const startTag = findParentTag(range.startContainer, tagName);

        if (startTag) {
            unwrap(startTag);
            return;
        }

        // 2. 선택 영역 안에 태그가 통째로 들어있는 경우 검사
        const selectedContent = range.cloneContents();
        const targetTag = selectedContent.querySelector(tagName);

        if (targetTag) {
            const realTag = content.querySelector(tagName);

            if (realTag) {
                unwrap(realTag);
                return;
            }
        }

        // 3. 없으면 새로 적용
        wrapSelection(tagName);
    }

    function findParentTag(node, tagName) {
        let current = node;

        if (current.nodeType === Node.TEXT_NODE) {
            current = current.parentNode;
        }

        while (current && current !== content) {
            if (
                current.nodeType === Node.ELEMENT_NODE &&
                current.tagName.toLowerCase() === tagName
            ) {
                return current;
            }

            current = current.parentNode;
        }

        return null;
    }

    function wrapSelection(tagName, attributes = {}) {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) return;

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) return;

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

        moveCursorAfter(wrapper);
    }

    function unwrap(element) {
        const parent = element.parentNode;
        const next = element.nextSibling;

        while (element.firstChild) {
            parent.insertBefore(element.firstChild, element);
        }

        parent.removeChild(element);

        const range = document.createRange();
        const selection = window.getSelection();

        if (next) {
            range.setStartBefore(next);
        } else {
            range.selectNodeContents(parent);
            range.collapse(false);
        }

        selection.removeAllRanges();
        selection.addRange(range);

        savedRange = range.cloneRange();
    }

    function applyStyleToSelection(styleName, value) {
        restoreSelection();

        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0 || selection.isCollapsed) return;

        const range = selection.getRangeAt(0);

        if (!content.contains(range.commonAncestorContainer)) return;

        const wrapper = document.createElement("span");
        wrapper.style[styleName] = value;

        const selectedContent = range.extractContents();

        wrapper.appendChild(selectedContent);
        range.insertNode(wrapper);

        moveCursorAfter(wrapper);
    }

    function moveCursorAfter(node)  {
        const range = document.createRange();
        const selection = window.getSelection();

        range.setStartAfter(node);
        range.collapse(true);

        selection.removeAllRanges();
        selection.addRange(range);

        savedRange = range.cloneRange();
    }

    /* ==========================================================
       Cursor Helpers
    ========================================================== */

    function removeLastSlash() {
        const selection = window.getSelection();

        if (!selection || selection.rangeCount === 0) return;

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

        if (!selection || selection.rangeCount === 0) return null;

        const range = selection.getRangeAt(0).cloneRange();
        let rect = range.getBoundingClientRect();

        if (rect.width === 0 && rect.height === 0) {
            const marker = document.createElement("span");
            marker.textContent = "\u200b";

            range.insertNode(marker);
            rect = marker.getBoundingClientRect();

            marker.parentNode.removeChild(marker);
        }

        return rect;
    }

    /* ==========================================================
       Outside Click
    ========================================================== */

    document.addEventListener("click", function (event) {
        if (!editor.contains(event.target)) {
            closeAllPopups();
        }
    });
});