# Report SSE

Le cartelle `baseline/` e `sse-improvements/` raccolgono i risultati prima e
dopo le ottimizzazioni. Ciascuna contiene una cartella per tool:

```text
baseline/                  sse-improvements/
  jmh/                       jmh/
  jmeter/                    jmeter/
  energibridge/              energibridge/
  creedengo/                 creedengo/
  websitecarbon/             websitecarbon/
  ecoindex/                  ecoindex/
  greenit-analysis/          greenit-analysis/
  fossa/                     fossa/
  guido/                     guido/
```

Salvare gli output originali e brevi note direttamente nella cartella del tool.
Usare nomi distinti per le diverse esecuzioni, per esempio
`baseline/jmh/2026-10-24-category-mapper.json` e una nota `.md` con lo stesso nome.
Non sono ancora presenti misurazioni: i file `.gitkeep` servono soltanto a
conservare le cartelle vuote in Git e possono essere rimossi quando arrivano
i primi risultati.

Per ogni esecuzione annotare data, commit eseguito, versione del tool, comando
o procedura, ambiente, dati e parametri. Usare gli stessi test e condizioni
equivalenti sulle due versioni. Per un'analisi di un sito, annotare anche URL,
pagina e versione effettivamente distribuita. Non sovrascrivere le misure
precedenti e non interpretare un tempo inferiore come prova di minore energia
senza una misura o una stima esplicitamente identificata.

I report della baseline possono essere aggiunti su `baseline` insieme ai test
e poi integrati in `master` tramite un branch di lavoro. I risultati delle
ottimizzazioni accompagnano la relativa pull request verso `master`.
Il confronto finale verrà riassunto nel [README principale](../README.md#tool-e-risultati).
