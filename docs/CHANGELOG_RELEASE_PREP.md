# Release Prep

**Datum:** 2026-04-04  
**Reviewer:** AI Senior Mod Developer  
**Status:** Ready for Release Candidate

---

## Scope

- Phase-7 QA-Dokument auf finalen Status gebracht.
- Dokumentation fuer Chest-IO-Feature angepasst (experimental, default off).
- Finale technische Release-Checks erneut ausgefuehrt.

---

## Finale Checks

1. `./gradlew test`  
Ergebnis: PASS

2. `./gradlew compileJava`  
Ergebnis: PASS

3. `./gradlew build`  
Ergebnis: PASS

4. `./gradlew runData`  
Ergebnis: PASS  
Hinweis: Datagen lief sauber durch, keine neuen Dateien geschrieben.

---

## Release-Relevante Hinweise

- Feature `Input/Output Chest Extension` ist als experimental konfigurierbar und standardmaessig deaktiviert:
  - Config-Key: `enableChestIoExtensionExperimental`
  - Default: `false`

- Manuelle Regressionstests:
  - Test 1-9: PASS
  - Test 10-11: Deferred (Post-Release, mit User abgestimmt)

---

## Restrisiko

- Last-/Stress-Verhalten unter Extrembedingungen (Netzwerk-Hardening, Performance-Smoke) ist nicht Teil des aktuellen Release-Gates.
- Risiko ist bekannt, dokumentiert und fuer diesen Release-Scope akzeptiert.

---

## Entscheidung

**Go fuer Release Candidate**, mit den dokumentierten Deferred-Tests als Post-Release-Aufgabe.
