const csrfHeader = document.querySelector("head meta[name='_csrf_header']")?.content
const csrfToken = document.querySelector("head meta[name='_csrf']")?.content
const roomPin = document.querySelector("head meta[name='room-pin']")?.content
const alertMessage = document.querySelector("#alert-message")
const checkedInAmount = document.querySelector("#checked-in-amount strong")

const requestRoomReset = async () => {
        const formData = new FormData();
        formData.append('roomPin', roomPin)
        const res = await fetch("reset", {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
            },
            body: formData,
        })
        const { success=false } = await res.json()
        return success
}

const resetCheckedInAmount = () => {
    checkedInAmount.innerText = 0
}

const showMessage = (message) => {
    alertMessage.style.display = "flex"
    alertMessage.innerText = message
    setTimeout(() => {
        alertMessage.style.display = "none"
        alertMessage.innerText = ""
    }, 3000)
}

document.getElementById("reset-room").onclick = async () => {
    try{
        if(!csrfHeader) throw new Error('csrf header not found')
        if(!csrfToken) throw new Error('csrf token not found')
        if(!roomPin) throw new Error('roomPin not found')
        if(confirm("Wollen Sie den Raum wirklich zurücksetzen?")){
            if(!await requestRoomReset()) throw new Error('internal server error')
            resetCheckedInAmount();
            showMessage("Der Raum wurde erfolgreich zurückgesetzt");
        }
    }catch(err){
        alert("Beim Zurücksetzen des Raumes ist ein Fehler aufgetreten.");
    }
}
