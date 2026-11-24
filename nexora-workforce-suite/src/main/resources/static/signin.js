const signinForm = document.getElementById("signinForm");
const signinButton = document.getElementById("signinButton");
const signinMessage = document.getElementById("signinMessage");

fetch("/csrf", { credentials: "same-origin" })
    .then(function(response) {
        if (!response.ok) {
            throw new Error("Unable to initialize secure sign in. Refresh to try again.");
        }
        return response.json();
    })
    .then(function(csrf) {
        document.getElementById("csrfToken").name = csrf.parameterName;
        document.getElementById("csrfToken").value = csrf.token;
        signinButton.disabled = false;
    })
    .catch(function(error) {
        signinMessage.textContent = error.message;
    });

if (new URLSearchParams(window.location.search).has("error")) {
    signinMessage.textContent = "The username or password is incorrect.";
} else if (new URLSearchParams(window.location.search).has("logout")) {
    signinMessage.textContent = "You have been signed out.";
}

signinForm.addEventListener("submit", function() {
    signinButton.disabled = true;
    signinButton.textContent = "Signing in...";
});
