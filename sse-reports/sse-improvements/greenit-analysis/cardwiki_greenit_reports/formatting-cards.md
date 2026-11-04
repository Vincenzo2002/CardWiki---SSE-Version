# GreenIT Report — Formatting Cards

## Pagina analizzata
- **URL:** `http://localhost:4200/help/markdown-syntax#markdown`
- **Ambiente:** build di produzione Angular, frontend servito localmente con `npx serve`; backend Spring Boot attivo.
- **Esito finale:** tutte le best practices GreenIT risultano verdi.

## Risultati finali
| Indicatore | Risultato |
|---|---:|
| EcoIndex | 86,21 |
| Classe EcoIndex | A |
| Consumo d'acqua stimato | 1,91 cl |
| Emissioni GHG stimate | 1,28 gCO₂e |
| Dimensione DOM | 329 |
| Richieste HTTP mostrate da GreenIT | 0 |
| Page Size mostrata da GreenIT | 0 KB |

> Nota: GreenIT mostra 0 richieste e 0 KB nello screenshot finale. Questi valori vengono riportati come visualizzati dallo strumento e non sono interpretati come prova dell'assenza di trasferimenti di rete.

## Interventi specifici e comuni
- Gli stili di `toasts-container` e `markdown-syntax` sono stati spostati nel foglio globale `styles.scss`.
- Sono stati rimossi i rispettivi `styleUrls` dai componenti e i fogli CSS specifici non più utilizzati.
- Aggiunta di un foglio di stile dedicato alla stampa (`assets/print.css`), collegato da `index.html` con `media="print"` e minificato.
- Configurazione locale degli header `Cache-Control` tramite `serve.json`: `index.html` usa `no-cache`, gli asset JS/CSS con hash usano `public, max-age=31536000, immutable` e gli asset in `assets/` usano `public, max-age=86400`.
- Verifica degli header con `curl` e tramite Chrome DevTools.

## Best practices
Tutti i controlli mostrati nello screenshot finale risultano verdi, inclusi Cache-Control, compressione, CSS/JS esterni e minificati, assenza di errori HTTP, print stylesheet e uso di HTTP/2 secondo il report GreenIT.

## Baseline e confronto
- **Baseline iniziale più attendibile:** EcoIndex 85,83 (la prima misurazione, 82,79, aveva 3 errori HTTP).
- **EcoIndex finale:** 86,21.
- **Variazione:** +0,38 punti.

La baseline più attendibile registrava 7 richieste e DOM 333; una successiva misurazione baseline riportava 10 richieste, 330 elementi DOM e 440 KB. Le misurazioni finali mostrano DOM 329. Richieste e Page Size finali sono mostrati come 0 da GreenIT e non sono direttamente confrontabili con i valori baseline.
