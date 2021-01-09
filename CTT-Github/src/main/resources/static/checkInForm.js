document.addEventListener("DOMContentLoaded", () => {
    const storage = window.localStorage;
    const emailText = document.getElementById("email-text");
    const postfixDropDown = document.getElementById("email-postfix");
    let emailPostfix = null;

    // Show content that requires JavaScript to function
    showJSEnabled();

    // Setup the initial drop-down selection and handle the drop down event
    postFixChanged(postfixDropDown.value);
    postfixDropDown.addEventListener("change", (event) => {
        postFixChanged(event.target.value);
    })

    // Inject the actual email into the final form submitted to the server
    document.getElementById("submit-form").addEventListener("submit", () => {
        const autoSignIn = document.getElementById("email-auto-sign-in");
        const combinedEmail = getFullEmail();

        if(autoSignIn.checked) {
            storage.setItem('email', combinedEmail);
        } else {
            storage.removeItem('email');
        }

        document.getElementById("submit-form-email").value = combinedEmail;
    })

    document.getElementById("submit-form-checkout").addEventListener("submit", () => {
        const autoSignIn = document.getElementById("email-auto-sign-in");
        const combinedEmail = getFullEmail();

        if(autoSignIn.checked) {
            storage.setItem('email', combinedEmail);
        } else {
            storage.removeItem('email');
        }

        document.getElementById("submit-form-email-checkout").value = combinedEmail;
    })

    // Change the form layout to allow the user to checkout of a previous room
    document.getElementById("checkout-button").addEventListener("click", () => doSignOut());

    // E-Mail input validation
    emailText.addEventListener("keypress", (ev) => {
        if(emailText.type === "number") {
            // For anything that's not a number prevent the input
            if(/\D/.test(ev.key)) {
                ev.preventDefault()
            }
        }
    });

    function postFixChanged(postfix) {
        switch (postfix) {
            case "student":
                emailText.placeholder = "Matrikelnummer";
                emailText.type = "number";
                emailPostfix = "@stud.hs-mannheim.de";
                break;
            case "internal":
                emailText.placeholder = "Nutzername";
                emailText.type = "text";
                emailPostfix = "@hs-mannheim.de";
                break;
            default:
            case "external":
                emailText.placeholder = "Vollständige E-Mail";
                emailText.type = "email";
                emailPostfix = null;
                break;
        }
    }

    function getFullEmail() {
        return emailText.value + emailPostfix;
    }

    function showJSEnabled() {
        document.querySelectorAll(".no-js").forEach((elem) => elem.classList.add("hidden"))
        document.querySelectorAll(".js-enabled").forEach((elem) => elem.classList.remove("js-enabled"))
    }

    function doSignOut() {
        document.querySelectorAll(".checkin-only").forEach((elem) => elem.classList.add("hidden"))
        document.querySelectorAll(".checkout-only").forEach((elem) => elem.classList.remove("checkout-only"))
    }
});