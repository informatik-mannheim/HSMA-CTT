document.addEventListener("DOMContentLoaded", () => {
    const storage = window.localStorage;
    const emailText = document.getElementById("email-text");
    const emailLabel = document.getElementById("email-text-label");
    const postFixRadioButtons = document.querySelectorAll("input[type=radio][name='email-postfix']");
    let emailPostfix = null;

    // Show content that requires JavaScript to function
    showJSEnabled();

    // Check if auto sign-in is enabled and if so, sign in
    autoSignIn();

    // Setup the initial radio selection and handle the change event
    postFixRadioButtons.forEach(radio => {
        radio.addEventListener('change', () => postFixChanged())
    });
    postFixChanged();

    // Inject the actual email into the final form submitted to the server
    document.getElementById("submit-form").addEventListener("submit", (ev) => {
        const autoSignIn = document.getElementById("email-auto-sign-in");
        const combinedEmail = getFullEmail();

        if (!validateEmail(combinedEmail)) {
            // Invalid email error
            ev.preventDefault();
            document.getElementById("invalid-email-error")
                .classList.remove("hidden");
            return;
        }

        if (autoSignIn.checked) {
            storage.setItem('email', combinedEmail);
        } else {
            storage.removeItem('email');
        }

        document.getElementById("submit-form-email").value = combinedEmail;
    })

    document.getElementById("submit-form-checkout").addEventListener("submit", () => {
        const autoSignIn = document.getElementById("email-auto-sign-in");
        const combinedEmail = getFullEmail();

        if (autoSignIn.checked) {
            storage.setItem("email", combinedEmail);
        } else {
            storage.removeItem("email");
        }

        document.getElementById("submit-form-email-checkout").value = combinedEmail;
    })

    // Change the form layout to allow the user to checkout of a previous room
    document.getElementById("checkout-button").addEventListener("click", () => doSignOut());

    // E-Mail input validation
    emailText.addEventListener("keypress", (ev) => {
        if (emailText.type === "number") {
            // For anything that's not a number prevent the input
            if (/\D/.test(ev.key)) {
                ev.preventDefault();
            }
        }
    });

    function postFixChanged() {
        let postfix = null;
        for (const radio of postFixRadioButtons) {
            if (radio.checked) {
                postfix = radio.value;
                break;
            }
        }

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

        if (emailPostfix !== null) {
            emailLabel.textContent = emailPostfix;
        } else {
            emailLabel.textContent = "";
        }
    }

    function getFullEmail() {
        return emailText.value + emailPostfix;
    }

    function showJSEnabled() {
        document.querySelectorAll(".no-js").forEach((elem) => elem.classList.add("hidden"));
        document.querySelectorAll(".js-enabled").forEach((elem) => elem.classList.remove("js-enabled"));
    }

    function doSignOut() {
        document.querySelectorAll(".checkin-only").forEach((elem) => elem.classList.add("hidden"));
        document.querySelectorAll(".checkout-only").forEach((elem) => elem.classList.remove("checkout-only"));
    }

    function autoSignIn() {
        const urlParams = new URLSearchParams(window.location.search);
        const noAutoSignInParam = urlParams.get('noautosignin');
        const storedEmail = storage.getItem("email");
        if (storedEmail !== null && !noAutoSignInParam) {
            document.getElementById("submit-form-email").value = storedEmail;
            document.getElementById("submit-form").submit();
        }
    }

    // https://stackoverflow.com/a/46181
    function validateEmail(email) {
        const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }
});