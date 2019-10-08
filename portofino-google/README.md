#Google OAuth 2 Configuration
Before you can use Google's OAuth2 authentication, you need to configure an api key

1. Connect to https://console.developers.google.com
1. Create a new project
1. Under the `OAuth consent screen` menu, verify that the domain is among those authorized
1. Under the `Credentials` menu, create new credentials of type `OAuth client ID` and type of application `Web application`;
   Under the `Authorized Javascript Origin` entry enter the URL of the application landing page, while as `Authorized redirec URIs` enter the URL of the landing page followed by the string `login?googleCallback` (es "http://localhost?login?googleCallback")
1. At this point insert the `client.id` and the `redirect uri` in the `portofino.properties`

#Configurazione Google OAuth2
Prima di poter utilizzare l'autenticazione OAuth2 di Google, bisogna configurare un api key

1. Collegarsi a https://console.developers.google.com
1. Creare un nuovo progetto
1. Sotto il menu `OAuth consent screen` verificare che il dominio sia tra quelli autorizzati
1. Sotto il menu `Credentials`, creare delle nuovo credenziali di tipo `Oauth client ID` e tipo di applicazione `Web application`;
 Alla voce `Authorized JavaScript Origin` inserire l'url della landing page dell'applicativo, mentre come `Authorized redirect URIs` inserire l'url della landing page seguito dalla stringa `login?googleCallback` (es "http://localhost/login?googleCallback")
1. A questo punto inserire il `client.id` e il `redirect uri` nel `portofino.properties`
