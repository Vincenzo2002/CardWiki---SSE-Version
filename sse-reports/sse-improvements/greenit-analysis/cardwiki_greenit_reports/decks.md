# GreenIT Report — Decks

## Pagina analizzata
- **URL:** `http://localhost:4200/search`
- **Ambiente:** build di produzione Angular, frontend servito localmente con `npx serve`; backend Spring Boot attivo.
- **Esito finale:** tutte le best practices GreenIT risultano verdi.

## Risultati finali
| Indicatore | Risultato |
|---|---:|
| EcoIndex | 95,18 |
| Classe EcoIndex | A |
| Consumo d'acqua stimato | 1,64 cl |
| Emissioni GHG stimate | 1,10 gCO₂e |
| Dimensione DOM | 73 |
| Richieste HTTP mostrate da GreenIT | 0 |
| Page Size mostrata da GreenIT | 0 KB |

> Nota: GreenIT mostra 0 richieste e 0 KB nello screenshot finale. Questi valori vengono riportati come visualizzati dallo strumento e non sono interpretati come prova dell'assenza di trasferimenti di rete.

## Interventi comuni applicati
- Spostamento degli stili dei componenti interessati nel foglio globale `styles.scss`, rimuovendo i fogli CSS specifici e i relativi `styleUrls`.
- Aggiunta di un foglio di stile dedicato alla stampa (`assets/print.css`), collegato da `index.html` con `media="print"` e minificato.
- Configurazione locale degli header `Cache-Control` tramite `serve.json`: `index.html` usa `no-cache`, gli asset JS/CSS con hash usano `public, max-age=31536000, immutable` e gli asset in `assets/` usano `public, max-age=86400`.
- Verifica degli header con `curl` e tramite Chrome DevTools.

## Best practices
Tutti i controlli mostrati nello screenshot finale risultano verdi, inclusi Cache-Control, compressione, CSS/JS esterni e minificati, assenza di errori HTTP, print stylesheet e uso di HTTP/2 secondo il report GreenIT.

## Baseline e confronto
- **EcoIndex baseline:** 90,32.
- **EcoIndex finale:** 95,18.
- **Variazione:** +4,86 punti.

La baseline iniziale aveva 16 richieste e DOM 86. La misurazione finale mostra DOM 73. I valori finali di richieste e dimensione pagina sono riportati come visualizzati da GreenIT (0 e 0 KB), pertanto non sono direttamente confrontabili con la baseline.
