# Note di sviluppo

## Da correggere

- Ridisegnare la tabella delle serie nel dettaglio dell'allenamento per evitare lo scorrimento orizzontale. I campi di ripetizioni previste, carico e ripetizioni effettuate devono essere comodi da usare sullo schermo del telefono.

## Realizzato – 16 novembre 2026

- I nomi degli esercizi si scrivono liberamente. Ogni esercizio mantiene il proprio ID; il catalogo predefinito e i relativi suggerimenti sono stati rimossi. L'archivio locale esistente dei nomi utente resta compatibile.
- Ogni serie può avere ripetizioni fisse o un intervallo. Note facoltative e tipo di serie (avvicinamento o lavoro) sono salvati sia nella scheda sia nella copia giornaliera.
- Il cronometro di recupero parte solo su richiesta e non blocca la schermata.
- Il riepilogo di un allenamento si esporta in PDF e si condivide con il menu Android. I valori mancanti vengono indicati chiaramente.
- I record delle schede e degli allenamenti sono ora scritti nei formati V7/W7; i lettori continuano ad accettare i formati precedenti.
- Verifiche: compilazione `:androidApp:assembleDebug` riuscita; su emulatore sono stati provati creazione e modifica di scheda, ripetizioni fisse/intervalli, note, serie di avvicinamento, timer, registrazione persistente e apertura del menu di condivisione PDF. I dati usati per la prova sono stati rimossi.
