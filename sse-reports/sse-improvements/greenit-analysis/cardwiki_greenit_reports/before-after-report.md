# CardWiki — GreenIT Before/After Report

## Scopo
Questo documento riepiloga gli interventi di ottimizzazione e i risultati GreenIT raccolti sulle sei pagine di CardWiki nell'ambiente locale.

## Ambiente di misurazione
- Frontend Angular compilato in modalità produzione.
- Frontend servito localmente tramite `npx serve` sulla porta 4200.
- Backend Spring Boot attivo con profilo `dev`.
- Analisi eseguite con GreenIT e best practices analysis attivata.

Le misurazioni sono state effettuate nell'ambiente locale. La configurazione descritta non deve essere interpretata come configurazione del deploy GitHub Pages.

## Interventi realizzati
### 1. Esternalizzazione degli stili dei componenti
Gli stili CSS dei componenti `toasts-container` e `markdown-syntax` sono stati spostati in `frontend/src/styles.scss`. Sono stati rimossi i rispettivi `styleUrls` dai file TypeScript e i fogli CSS specifici non più utilizzati. Dopo la modifica, il numero di elementi `style` osservati nel DOM è sceso da 3 a 1 (l'elemento residuo era vuoto).

### 2. Foglio di stile per la stampa
È stato creato `frontend/src/assets/print.css`, collegato in `frontend/src/index.html` con `media="print"`. La regola `@media print` precedentemente aggiunta al foglio globale è stata sostituita da un foglio dedicato, riconosciuto da GreenIT. Il file è stato successivamente minificato.

### 3. Cache-Control nell'ambiente locale
È stato aggiunto `frontend/serve.json` e copiato nella cartella di build pubblicata da `serve`. Le regole impostano:
- `index.html`: `Cache-Control: no-cache`;
- file JavaScript e CSS: `Cache-Control: public, max-age=31536000, immutable`;
- risorse sotto `assets/`: `Cache-Control: public, max-age=86400`.

Gli header sono stati verificati con `curl` su tutte le risorse segnalate da GreenIT e nel pannello Network di Chrome DevTools. Dopo una nuova analisi GreenIT, il controllo Cache-Control è risultato verde.

### 4. Build e verifica
La build di produzione è stata generata con `NODE_OPTIONS=--openssl-legacy-provider npm run build:prod`. Il frontend è stato servito localmente con `npx serve -s dist/cardwiki -l 4200`. I controlli GreenIT delle cinque pagine principali e della pagina di dettaglio del deck sono stati verificati dopo gli interventi.

## Confronto EcoIndex

| Pagina | EcoIndex baseline | EcoIndex finale | Variazione | DOM finale |
|---|---:|---:|---:|---:|
| About | 90,05 | 95,00 | +4,95 | 75 |
| Categories | 93,13* | 95,98 | +2,85* | 64 |
| Decks (ricerca) | 90,32 | 95,18 | +4,86 | 73 |
| Formatting Cards | 85,83** | 86,21 | +0,38 | 329 |
| Users | 90,64 | 95,36 | +4,72 | 71 |
| Super Large Test Deck | N/D | 63,05 | N/D | 1.048 |

- *Categories: la baseline riportava 0 richieste; la misurazione è considerata non affidabile. La variazione è puramente indicativa e non va usata come prova quantitativa.*
- **Formatting Cards: la baseline iniziale di 82,79 presentava 3 errori HTTP. È stata sostituita, per il confronto, dalla successiva baseline di 85,83 con 0 errori HTTP.
- Super Large Test Deck: è disponibile solo il risultato post-intervento.

## Altri indicatori e limiti dei dati
Negli screenshot finali GreenIT mostra 0 richieste HTTP e 0 KB di Page Size per tutte le sei pagine. Questi valori sono riportati fedelmente, ma non sono confrontabili con le baseline precedenti, che mostravano richieste e dimensioni non nulle. Non si deduce quindi una riduzione reale a zero del traffico o del peso trasferito.

Il numero di elementi DOM finali è invece disponibile negli screenshot: varia da 64 a 1.048. Il valore più alto è quello della pagina Super Large Test Deck.

## Esito delle best practices
Per tutte e sei le pagine, gli screenshot finali mostrano verdi i controlli GreenIT visualizzati. Tra i controlli verificati figurano:
- header Expires o Cache-Control;
- compressione delle risorse;
- limite del numero di domini e richieste;
- assenza di errori HTTP;
- esternalizzazione e minificazione CSS/JS;
- assenza di cookie per risorse statiche;
- assenza di redirect;
- ottimizzazione delle immagini bitmap e SVG, ove applicabile;
- presenza di print stylesheet;
- limite al numero di fogli di stile;
- HTTP/2;
- uso di font standard.

Per Super Large Test Deck l'EcoIndex resta in classe C nonostante i controlli verdi. La pagina ha 1.048 elementi DOM; non è stata modificata ulteriormente.

## Conclusione
Gli interventi hanno permesso di ottenere esiti verdi per tutte le best practices GreenIT mostrate sulle sei pagine analizzate. Le cinque pagine con baseline disponibile presentano un EcoIndex finale superiore al valore baseline registrato, con la riserva relativa alla misurazione iniziale di Categories e alla baseline scelta per Formatting Cards. La pagina Super Large Test Deck è stata documentata come misurazione post-intervento, senza confronto pre/post.
