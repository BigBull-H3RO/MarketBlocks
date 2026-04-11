# Phase 7: QA and Regression

**Review-Datum:** 2026-04-04  
**Reviewer:** AI Senior Mod Developer  
**Status:** Abgeschlossen (vereinbarter Scope)

---

## Ziel

Phase 7 validiert, dass die Aenderungen aus Phase 1-6 keine funktionalen Regressionen oder Release-Blocker erzeugen.

---

## Automatische Checks

Die finalen technischen Release-Checks werden im Release-Prep (separates Dokument) erneut ausgefuehrt und dort protokolliert.

---

## Manuelle Regression-Matrix (Finalstatus)

1. **SmallShop Offer Lifecycle**
- Setup: Owner erstellt Angebot, loescht Angebot, erstellt erneut.
- Erwartung: Keine Ghost-Offers, keine falschen Buttons/Zustaende, Vorschau korrekt.
- Status: PASS

2. **SmallShop Kauf-Atomizitaet**
- Setup: Kaeufer fuehrt mehrere Einzelkaeufe und Shift-Click-Bulk-Kaeufe aus.
- Erwartung: Kein Itemverlust, keine Duplikation, Zahlung exakt gemaess Offer.
- Status: PASS

3. **Output-Full/Out-of-Stock Guardrails**
- Setup: Output kuenstlich fuellen, Input leeren, danach Kaufversuche.
- Erwartung: Korrekte Fehlermeldungen, keine Teiltransaktionen.
- Status: PASS

4. **AutoFill Payment**
- Setup: AutoFill mit passenden/nicht passenden Items im Spielerinventar.
- Erwartung: Nur gueltige Items uebertragen, keine fremden Slots manipuliert.
- Status: PASS

5. **Owner-/Permission-Grenzen**
- Setup: Non-Owner versucht Template-/Settings-/Owner-Aktionen.
- Erwartung: Zugriff strikt blockiert, Owner-Pfade funktionieren.
- Status: PASS
- Hinweis: Owner-Listenverwaltung wurde auf Haupt-Owner begrenzt (Additional Owner darf Liste nicht sehen/aendern).

6. **Input/Output Chest Extension**
- Setup: SideModes wechseln, Nachbartruhen verbinden/trennen, Chunk reload.
- Erwartung: Cache aktualisiert korrekt, kein Desync/Leak, Transfer stabil.
- Status: PASS
- Hinweis: Feature ist jetzt als `enableChestIoExtensionExperimental` konfigurierbar und standardmaessig deaktiviert.

7. **Redstone/Comparator Verhalten**
- Setup: Kaeufe mit aktivem Redstone, Comparator-Abfrage auf Input/Output-Seiten.
- Erwartung: Pulse/Signalwerte konsistent, keine Dauer-Power-Bugs.
- Status: PASS

8. **NBT/Chunk Reload**
- Setup: Shop konfigurieren, Welt neu laden, Chunk unload/reload.
- Erwartung: Offer/Owner/Settings bleiben korrekt; keine Client-NBT-Korruption.
- Status: PASS

9. **ServerShop Multi-Viewer Sync**
- Setup: 2-5 Spieler mit offenem ServerShop, parallel Offer-Aenderungen/Kaeufe.
- Erwartung: Seiten/Offers synchron, keine haengenden Menues, keine Inkonsistenz.
- Status: PASS

10. **Netzwerk-Hardening**
- Setup: Grenzfaelle (grosse Owner-Listen, ungueltige Inputs, schnelle Tab/Packet-Folgen).
- Erwartung: Keine Crashes, invalides Input wird sicher verworfen/geclamped.
- Status: Deferred (Post-Release)

11. **Performance Smoke**
- Setup: Viele Offers + mehrere gleichzeitige Viewer + wiederholte Kaeufe.
- Erwartung: Keine auffaelligen Tick-Spikes, keine Lock-bedingten Haenger.
- Status: Deferred (Post-Release)

---

## Scope-Entscheidung

- Mit dem User abgestimmt: Tests 10 und 11 werden in Post-Release-Validierung verschoben.
- Phase 7 gilt damit im vereinbarten Release-Scope als abgeschlossen.

---

## Restrisiko

- Last- und Stress-Risiken (Netzwerk-Spam, Performance-Spikes) bleiben bis zur Post-Release-Validierung bestehen.
- Dieses Restrisiko ist bekannt und akzeptiert.
