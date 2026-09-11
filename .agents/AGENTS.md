# MarketBlocks – Agent-Regeln

## Sprache

- Kommuniziere immer auf **Deutsch** mit dem Benutzer.
- Code, Kommentare im Code und Commit-Messages bleiben auf **Englisch**.

## Projekt-Kontext

- Dies ist ein **NeoForge Minecraft Mod** für Minecraft 1.21.x.
- Mod-ID: `marketblocks`
- Java-Version: **21**
- Build-System: **Gradle** mit NeoForge Gradle Plugin
- Hauptpaket: `de.bigbull.marketblocks`

## Projektstruktur – Nicht ohne Genehmigung ändern

Die folgenden Dateien und Verzeichnisse sind **kritische Infrastruktur**. Ändere sie **nur**, wenn der Benutzer es ausdrücklich verlangt oder der genehmigte Plan es vorsieht:

- `build.gradle` und `settings.gradle`
- `gradle.properties`
- `MarketBlocks.java` (Hauptmod-Klasse)
- `MarketBlocksClient.java`
- `core/init/RegistriesInit.java` (Registrierungen)
- `core/init/CreativeTabInit.java`
- `core/config/Config.java`
- `network/NetworkHandler.java`
- `data/DataGenerators.java`

## Paketstruktur – Konventionen einhalten

Halte dich strikt an die bestehende Paketstruktur:

```
de.bigbull.marketblocks/
├── core/           → Config, Init, Events, Commands, Data-Klassen
├── feature/        → Feature-Module (marketplace, trader, singleoffer, etc.)
│   └── <feature>/
│       ├── client/ → Client-seitige Klassen (Screens, Renderer)
│       ├── data/   → Feature-spezifische Daten
│       └── network/→ Network Packets
├── data/           → Data Generators (Lang, Loot, Recipes, Tags, etc.)
├── network/        → Globaler NetworkHandler
├── client/         → Globale Client-Klassen
├── compat/         → Mod-Kompatibilität (Jade, JourneyMap, etc.)
└── util/           → Hilfsfunktionen
```

- Erstelle **keine neuen Top-Level-Pakete** ohne Genehmigung.
- Neue Features gehören als Unterpaket in `feature/`.

## Code-Stil

- Verwende den bestehenden Code-Stil des Projekts als Referenz.
- Alle Klassen brauchen das korrekte `package`-Statement.
- Verwende `MarketBlocks.MODID` statt hartcodierter Strings für die Mod-ID.
- Verwende `MarketBlocks.LOGGER` für Logging.
- Behalte alle bestehenden Kommentare und Dokumentation bei, sofern sie nicht direkt von der Änderung betroffen sind.

## Änderungs-Regeln

> [!CAUTION]
> Diese Regeln sind verbindlich und dürfen NICHT ignoriert werden.

1. **Keine unnötigen Änderungen.** Ändere nur Dateien, die für die aktuelle Aufgabe relevant sind. Kein Refactoring "nebenbei".
2. **Keine Dateien löschen** ohne ausdrückliche Genehmigung des Benutzers.
3. **Keine Abhängigkeiten hinzufügen/entfernen** ohne ausdrückliche Genehmigung.
4. **Keine bestehende Funktionalität entfernen oder umschreiben**, die nicht Teil der Aufgabe ist.
5. **Keine Registrierungen ändern** (Blöcke, Items, Entities, Menus, etc.) ohne dass es im genehmigten Plan steht.
6. **Immer den genehmigten Plan einhalten.** Wenn du während der Umsetzung merkst, dass zusätzliche Änderungen nötig sind, stoppe und frage den Benutzer.

## Build & Test

- Verwende `gradlew build` zum Bauen (nicht `gradlew.bat` – das System wählt automatisch).
- Prüfe nach Änderungen, ob das Projekt kompiliert.
- Datagen läuft über `gradlew runData`.

## Kompatibilität

- Jade-Integration: `compat/` Paket
- JourneyMap-Integration: `compat/` Paket
- Neue Mod-Kompatibilitäten gehören ebenfalls in `compat/`.

## Kommunikation und Antwortverhalten

Bevor du antwortest, befolge immer diese Regeln:
1. Wenn meine Anfrage unklar ist: Stell Rückfragen, statt zu raten.
2. Nenne deine Annahmen offen - ich will wissen, wovon du ausgehst.
3. Sei ehrlich, nicht nett. Wenn meine Idee Schlecht ist, sag es - mit Begründung.
4. Denk Schritt für Schritt und zeig deinen Denkweg.
5. Sei konkret: Beispiel, Zahlen, umsetzbare Schritte - keine vagen Tipps.
6. Wenn du etwas nicht sicher weisst, sag es. Lieber ehrlich unsicher als falsch sicher.
7. Hinterfrage meine Prämisse, wenn sie falsch sein könnte. ich will die richtige Antwort, nicht die bequeme.
8. Komm auf den Punkt. Kein Fülltext, keine Wiederholungen, keine unnötigen Einleitungen.
