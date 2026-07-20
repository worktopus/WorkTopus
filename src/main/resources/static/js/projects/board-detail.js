function showCommentEdit(id) {
    const content = document.getElementById(`comment-content-${id}`);
    const editForm = document.getElementById(`comment-edit-${id}`);
    const actions = document.getElementById(`comment-actions-${id}`);

    if (!content || !editForm) {
        return;
    }

    content.style.display = "none";
    editForm.style.display = "block";

    if (actions) {
        actions.style.display = "none";
    }
}

function hideCommentEdit(id) {
    const content = document.getElementById(`comment-content-${id}`);
    const editForm = document.getElementById(`comment-edit-${id}`);
    const actions = document.getElementById(`comment-actions-${id}`);

    if (!content || !editForm) {
        return;
    }

    content.style.display = "block";
    editForm.style.display = "none";

    if (actions) {
        actions.style.display = "flex";
    }
}