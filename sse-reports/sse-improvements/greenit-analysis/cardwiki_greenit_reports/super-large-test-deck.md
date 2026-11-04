# GreenIT Report — Super Large Test Deck

## Pagina analizzata
- **Nome:** Super Large Test Deck
- **URL:** `http://localhost:4200/decks/4`
- **Ambiente:** build di produzione Angular, frontend servito localmente con `npx serve`; backend Spring Boot attivo.
- **Tipo di misurazione:** solo post-intervento. Non è disponibile una baseline iniziale.

## Risultati finali
| Indicatore | Risultato |
|---|---:|
| EcoIndex | 63,05 |
| Classe EcoIndex | C |
| Consumo d'acqua stimato | 2,61 cl |
| Emissioni GHG stimate | 1,74 gCO₂e |
| Dimensione DOM | 1.048 |
| Richieste HTTP mostrate da GreenIT | 0 |
| Page Size mostrata da GreenIT | 0 KB |

> Nota: GreenIT mostra 0 richieste e 0 KB nello screenshot finale. Questi valori vengono riportati come visualizzati dallo strumento e non sono interpretati come prova dell'assenza di trasferimenti di rete.

## Best practices
Tutti i controlli mostrati nello screenshot post-intervento risultano verdi. Il dettaglio di “Externalize css and js” segnala 2 inline stylesheet(s) e inline JavaScript(s), pur mantenendo verde il controllo secondo il report.

## Osservazioni
L'EcoIndex è in classe C. La pagina presenta un DOM di 1.048 elementi, nettamente superiore alle altre pagine analizzate (da 64 a 329 elementi DOM). Il valore elevato del DOM è un possibile fattore da approfondire, ma non è stata svolta un'analisi causale né sono state apportate modifiche specifiche a questa pagina, come richiesto.

## Baseline e confronto
- **EcoIndex baseline:** 58,44.
- **EcoIndex finale:** 63,05.
- **Variazione:** +4,61 punti.

La baseline iniziale aveva 16 richieste e DOM 1058. La misurazione finale mostra DOM 1048. I valori finali di richieste e dimensione pagina sono riportati come visualizzati da GreenIT (0 e 0 KB), pertanto non sono direttamente confrontabili con la baseline.
