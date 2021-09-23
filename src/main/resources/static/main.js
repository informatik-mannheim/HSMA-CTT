
const checkInAlertForm = document.getElementById('check-in-alert-form')

checkInAlertForm.onsubmit = e => {
    if(!confirm("Wollen Sie sich wirklich auschecken?")){
        e.preventDefault();
    }
}