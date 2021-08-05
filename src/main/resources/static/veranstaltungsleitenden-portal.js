const csrfHeader = document.querySelector("head meta[name='_csrf_header']")?.content
const csrfToken = document.querySelector("head meta[name='_csrf']")?.content
const roomPin = document.querySelector("head meta[name='room-pin']")?.content

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
        const { success } = await res.json()
        return success
}

document.getElementById("reset-room").onclick = async () => {
    try{
        if(!csrfHeader) throw new Error('csrf header not found')
        if(!csrfToken) throw new Error('csrf token not found')
        if(!roomPin) throw new Error('roomPin not found')
        if(confirm("Wollen Sie wirklich den Raum zurücksetzen?")){
            if(await requestRoomReset()) {
                location.reload();
            }
        }
    }catch(err){
        console.err(err)
    }
}