let tickets = document.getElementById("tickets-assigned-to-you");
if(page.authenticationService.currentUser) {
  page.translate.get(tickets.innerText).subscribe(t => {
    tickets.innerText = t;
  });
  page.http.get(page.computeSourceUrl() + "/active-tickets-assigned-to-me").subscribe(ts => {
    if(ts && ts.length > 0) {
      tickets.innerText = '';
      const list = document.createElement("ul");
      tickets.appendChild(list);
      ts.forEach(t => {
        let item = document.createElement('li');

        let link = document.createElement("a");
        let projectId = page.parent.id;
        let baseHref = document.querySelector("base").href;
        link.href = `${baseHref}${page.parent.url}/tickets/${projectId}/${t.n}`.replaceAll(/([^:])\/\//g, "$1/");
        link.innerText = `${projectId}-${t.n}`;
        link.style.fontWeight = "bold";
        item.appendChild(link);

        let title = document.createElement("strong");
        title.innerText = " " + t.title;
        item.appendChild(title);

        let lastUpdated = document.createElement("small");
        page.translate.get("Last updated on").subscribe(text => {
          lastUpdated.innerText = text + " " + moment(t.last_updated, 'yyyy-MM-dd HH:mm:ss z');
        });

        list.appendChild(item);
      });
    }
  })
} else {
  let noTicketsBecauseGuest = document.getElementById("no-tickets-assigned-to-you-because-guest");
  page.translate.get(noTicketsBecauseGuest.innerText).subscribe(t => {
    noTicketsBecauseGuest.innerText = t;
  });
  tickets.hidden = true;
  noTicketsBecauseGuest.hidden = false;
}
