page.htmlLoadStatus.subscribe(s => {
  if(s === 1) {
    let noTickets = document.getElementById("no-tickets-assigned-to-you");
    page.translate.get(noTickets.innerText).subscribe(t => {
      noTickets.innerText = t;
    });
  }
})
