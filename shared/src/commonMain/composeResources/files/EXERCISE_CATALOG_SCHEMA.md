# Schema del catalogo esercizi (v2)

Il catalogo incluso nell'app è una risorsa JSON di sola lettura. La radice contiene `schema_version` e `exercises`.

Ogni esercizio contiene:

- `id`: identità stabile usata per selezione e storico; non cambiarla per rinominare una voce.
- `family_id`: identità condivisa dalle varianti correlate; per una famiglia nuova può coincidere con il suo nome stabile.
- `name_en` e `name_it`: nomi canonici inglese e italiano. Se un termine internazionale è quello comunemente usato anche in italiano, i due valori possono coincidere.
- `aliases_en` e `aliases_it`: grafie e nomi alternativi ricercabili, senza creare identità aggiuntive.
- `variant` (facoltativo): solo gli attributi che distinguono la variante e ne giustificano uno storico separato. Esempi attuali: `equipment` e `bench_angle_degrees`.

Le varianti con ID differenti possono condividere la stessa famiglia. Non riutilizzare un ID per un movimento differente e non fondere voci solo perché condividono alias generici. La ricerca ignora maiuscole, spazi e punteggiatura; termini comuni che corrispondono a più voci devono continuare a mostrare tutte le scelte.

Gli esercizi personalizzati sono archiviati separatamente sul dispositivo nel formato locale CE1 e non vengono scritti in questo file. Il lettore conserva anche il supporto al precedente array v1 per installazioni o risorse non aggiornate.
