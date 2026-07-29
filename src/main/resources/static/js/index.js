document.addEventListener("DOMContentLoaded", function () {
    const revealElements = document.querySelectorAll(".reveal");

    console.log("reveal 요소 개수:", revealElements.length);

    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            console.log(entry.target, entry.isIntersecting);

            if (entry.isIntersecting) {
                entry.target.classList.add("show");
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0
    });

    revealElements.forEach(function (element) {
        observer.observe(element);
    });
});