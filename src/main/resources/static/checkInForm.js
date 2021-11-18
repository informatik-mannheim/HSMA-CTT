window.addEventListener("pageshow", () => {
    const storage = window.localStorage;
    const emailText = document.getElementById("email-text");
    const guestFreeSection = document.getElementsByClassName("guest-free-section")
    const emailLabel = document.getElementById("email-text-label");
    const postFixRadioButtons = document.querySelectorAll("input[type=radio][name='email-postfix']");
    let emailPostfix = null;

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
        if(postFixRadioButtons[3].checked){
            if(checkForEmptyGuestFields() != ""){
                ev.preventDefault();
                document.getElementById("invalid-email-error").getElementsByTagName("h4")[0].innerText = checkForEmptyFields();
                document.getElementById("invalid-email-error").classList.remove("hidden");
                return;
            }
        }
        const firstname = document.getElementById("guest-first-name").value ;
        const surname = document.getElementById("guest-surname").value;
        const name = firstname +" "+ surname;
        document.getElementById("submit-form-email").value = combinedEmail;
        document.getElementById("submit-form-name").value = name;
        document.getElementById("submit-form-number").value = document.getElementById("guest-number").value;
        document.getElementById("submit-form-address").value = address;
    })

    document.getElementById("submit-form-checkout").addEventListener("submit", (ev) => {
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
                for (let i = 0; i < guestFreeSection.length; i++) {
                    guestFreeSection[i].style.visibility = "hidden";
                    guestFreeSection[i].style.maxHeight = "0";
                }
                break;
            case "internal":
                emailText.placeholder = "Nutzername";
                emailText.type = "text";
                emailPostfix = "@hs-mannheim.de";
                for (let i = 0; i < guestFreeSection.length; i++) {
                    guestFreeSection[i].style.visibility = "hidden";
                    guestFreeSection[i].style.maxHeight = "0";
                }
                break;
            case "internal2":
                emailText.placeholder = "Nutzername";
                emailText.type = "text";
                emailPostfix = "@lba.hs-mannheim.de";
                for (let i = 0; i < guestFreeSection.length; i++) {
                    guestFreeSection[i].style.visibility = "hidden";
                    guestFreeSection[i].style.maxHeight = "0";
                }
                break;
            default:
            case "external":
                emailText.type = "email";
                emailPostfix = null;
                for (let i = 0; i < guestFreeSection.length; i++) {
                    guestFreeSection[i].style.visibility = "visible";
                    guestFreeSection[i].style.maxHeight = "100%";
                }
                break;
        }

        if (emailPostfix !== null) {
            emailLabel.textContent = emailPostfix;
            emailLabel.style.paddingLeft = "5px";
            emailLabel.style.width = "80%";
        } else {
            emailLabel.textContent = "";
            emailLabel.style.width = "0%";
        }
    }

	function getFullEmail() {
        // Check if the postfix is null
        const emailTextValue = emailText.value.trim()
        if (!!emailPostfix) {
            return emailTextValue + emailPostfix;
        } else {
            return emailTextValue;
        }
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
    function checkForEmptyGuestFields(){
        if(document.getElementById("guest-first-name").value == ""){
            return "Unvollständiger Vorname.";
        }
        if(document.getElementById("guest-surname").value == ""){
            return "Unvollständiger Nachname.";
        }
        if(document.getElementById("guest-number").value == ""){
            return "Ungültige Telefonnummer.";
        }
        if(document.getElementById("guest-address-1").value == ""){
            return "Unvollständige Straßeneingabe.";
        }
        if(document.getElementById("guest-address-2").value == ""){
            return "Unvollständige Ortseingabe.";
        }
        if(document.getElementById("guest-address-3").value == ""){
            return "Unvollständige Ländereingabe.";
        }
        return "";
    }
    // https://stackoverflow.com/a/46181
    function validateEmail(email) {
        const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }
});
