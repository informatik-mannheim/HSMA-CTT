document.addEventListener("DOMContentLoaded", () => {

    const faqList=[
        {"question": "Wie kann ich mich einchecken?", "answer": "Du musst lediglich mit deinem Smartphone den QR-Code abscannen, dann wirst du automatisch zum Check in für den passenden Raum weitergeleitet. Alternativ, wenn du keinen QR-Scanner zur Verfügung hast, kannst du auch manuell die URL in einen beliebigen Browser eingeben. Eine etwas ausführlichere Antwort findest du ", "link": "https://ctt.hs-mannheim.de/howToQr"},
        {"question": "Wie kann ich mich auschecken?", "answer": "Das Auschecken funktioniert genau so, wie das Einchecken. Du musst lediglich den QR-Code abscannen und dann ganz unten auf den Button Abmelden klicken. Dann wirst du automatisch zum Check-out weitergeleitet. Eine ausführliche Antwort findest du ", "link": "https://ctt.hs-mannheim.de/howToQr"},
        {"question": "Ich habe kein Smartphone zur Verfügung, wie kann ich mich trotzdem einchecken?", "answer": "Auf jedem Zettel befindet sich oberhalb des QR Codes eine URL. Du kannst auch einfach in deinem Browser die URL eintippen und dich so beispielsweise mit deinem Laptop oder Tablet einchecken. Falls du gar kein Gerät zur Verfügung hast bitte jemand anderen dich einzuchecken. Eine ausführliche Anleitung dazu findest du ", "link": "https://ctt.hs-mannheim.de/howToInkognito"},
        {"question": "Kann ich auch andere einchecken?", "answer": "Ja, das geht natürlich auch, per Handy, Tablet oder Laptop. Öffne dazu einfach ein weiteres Fenster in deinem Browser und rufe die URL auf dem Zettel auf. Anschließend, einfach die Daten der Person eintragen. Der Einfachheitshalber kannst du das Fenster für die Veranstaltungsdauer direkt für das Auschecken offen lassen. Eine etwas ausführlichere Beschreibung findest du ", "link": "https://ctt.hs-mannheim.de/howToInkognito"},
        {"question": "Kann ich mich auch später ein- bzw. auschecken?", "answer": "Nein, es ist sehr wichtig dass das ein uns auschecken auch immer genau dann passiert, wenn du den Raum betrittst oder verlässt, sonst werden im System falsche Uhrzeiten gespeichert."},
        {"question": "Kann ich auch ohne Kontaktverfolgung an Veranstaltungen der Hochschule teilnehmen ?", "answer": "Nein,  leider geht das aktuell nur bei Online Veranstaltungen."},
        {"question": "Was ist, wenn ich vergessen habe mich auszuchecken?", "answer": "Du wirst automatisch ausgeheckt, wenn du einen anderen Raum betrittst. Notfalls checkt dich das System abends aus. Für die Kontaktverfolgung warst du dann leider noch im Raum."},
    ];
    let temp = document.querySelector("template");
    let container = document.getElementById("faq-page-container");
    for(let i = 0; i<faqList.length; i++){
        const button = temp.content.querySelector("button");
        button.innerHTML=faqList[i].question+`<i class="fa fa-plus-square"></i>`;
        button.id="collapseButtonFaq"+i;
        button.setAttribute("data-target", "#faq-target-"+i);
        const faqAnswerDiv =  temp.content.querySelector(".collapse");
        faqAnswerDiv.id= "faq-target-"+i;
        let text = document.createElement("p");
        text.innerText= faqList[i].answer;
        if(faqList[i].link !== undefined){
            let link = document.createElement("a");
            link.innerText="hier.";
            link.href=faqList[i].link
            text.appendChild(link);
        };
        if(faqList[i].mail !== undefined){
           let link = document.createElement("a");
           link.innerText="cocona.zv@hs-mannheim.de";
           link.href=faqList[i].mail
           text.insertAdjacentHTML('beforeend', "the text");
         };
        faqAnswerDiv.innerHTML="";
        faqAnswerDiv.appendChild(text);
        let clon = temp.content.cloneNode(true);
        container.appendChild(clon);
    }
})