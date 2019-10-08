#Configurazione Google OAuth2
Prima di poter utilizzare l'autenticazione OAuth2 di Google, bisogna configurare un api key

1. Collegarsi a https://console.developers.google.com
1. Creare un nuovo progetto
1. Sotto il menu `OAuth consent screen` verificare che il dominio sia tra quelli autorizzati
1. Sotto il menu `Credentials`, creare delle nuovo credenziali di tipo `Oauth client ID` e tipo di applicazione `Web application`;
 Alla voce `Authorized JavaScript Origin` inserire l'url della landing page dell'applicativo, mentre come `Authorized redirect URIs` inserire l'url della landing page seguito dalla stringa `login?googleCallback` (es "http://localhost/login?googleCallback")
1. A questo punto inserire il `client.id` e il `redirect uri` nel `portofino.properties`
