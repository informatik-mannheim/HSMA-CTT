document.addEventListener("DOMContentLoaded", () => {

    let faqList=[
        {"frage": "Wie kann ich mich einchecken?", "antwort": "Du musst lediglich mit deinem Smartphone den QR-Code abscannen, dann wirst du automatisch zum Check in für den passenden Raum weitergeleitet. Alternativ, wenn du keinen QR-Scanner zur Verfügung hast, kannst du auch manuell die URL in einen beliebigen Browser eingeben. Eine etwas ausführlichere Antwort findest du ", "link": "https://ctt.hs-mannheim.de/howToQr"},
        {"frage": "Wie kann ich mich auschecken?", "antwort": "Das Auschecken funktioniert genau so, wie das Einchecken. Du musst lediglich den QR-Code abscannen und dann ganz unten auf den Button Abmelden klicken. Dann wirst du automatisch zum Check-out weitergeleitet. Eine ausführliche Antwort findest du ", "link": "https://ctt.hs-mannheim.de/howToQr"},
        {"frage": "Ich habe kein Smartphone zur Verfügung, wie kann ich mich trotzdem einchecken?", "antwort": "Auf jedem Zettel befindet sich oberhalb des QR Codes eine URL. Du kannst auch einfach in deinem Browser die URL eintippen und dich so beispielsweise mit deinem Laptop oder Tablet einchecken. Falls du gar kein Gerät zur Verfügung hast bitte jemand anderen dich einzuchecken. Eine ausführliche Anleitung dazu findest du ", "link": "https://ctt.hs-mannheim.de/howToInkognito"},
        {"frage": "Kann ich mich auch später ein- bzw. auschecken?", "antwort": "Nein, es ist sehr wichtig dass das ein uns auschecken auch immer genau dann passiert, wenn du den Raum betrittst oder verlässt, sonst werden im System falsche Uhrzeiten gespeichert."},
        {"frage": "Ich habe Corona und war in den vergangenen Tagen an der Hochschule, was muss ich jetzt tun?", "antwort": "antwort5"},
    ]
    let temp = document.querySelector("template");
    let container = document.getElementById("faq-page-container");
    for(let i = 0; i<faqList.length; i++){
        const button = temp.content.querySelector("button");
        button.innerHTML=faqList[i].frage+`<i class="fa fa-plus-square"></i>`;
        button.id="collapseButtonFaq"+i;
        button.setAttribute("data-target", "#faq-target-"+i);
        const faqAnswerDiv =  temp.content.querySelector(".collapse");
        faqAnswerDiv.id= "faq-target-"+i;
        let text = document.createElement("p");
        text.innerText= faqList[i].antwort;
        if(faqList[i].link !== undefined){
            let link = document.createElement("a");
            link.innerText="hier.";
            link.href=faqList[i].link
            text.appendChild(link);
        };
        faqAnswerDiv.innerHTML="";
        faqAnswerDiv.appendChild(text);
        let clon = temp.content.cloneNode(true);
        container.appendChild(clon);
    }
})