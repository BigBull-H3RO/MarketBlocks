# Commands & Permissions

Diese Übersicht gruppiert die wichtigsten Commands und Rechte nach Rollen.

## Admin / OP

| Command | Zweck |
| --- | --- |
| `/marketblocks adminmode [true|false]` | Aktiviert/deaktiviert den globalen Admin/Edit-Modus. |
| `/marketblocks marketplace` | Öffnet den Marketplace. |
| `/marketblocks marketplace reload` | Lädt die Marketplace-Konfiguration neu. |
| `/marketblocks marketplace resetlimits <player>` | Setzt Daily-Limits für einen Spieler zurück. |

## Spieler

- Zugriff auf Kauf-Workflows gemäß Shop-/Marketplace-Zustand
- keine administrativen Marketplace-Mutationen ohne passende Rechte

## Permission-Nodes (Kurz erklärt)

Die konkreten Permission-Nodes können je nach Server-Setup/Permission-Mod variieren.

Empfehlung:

1. Rechte zuerst über OP/Admin testen.
2. Danach in Gruppen (z. B. Admin, Mod, Spieler) sauber trennen.
3. Kritische Befehle (`reload`, `resetlimits`, `adminmode`) nur vertrauenswürdigen Rollen geben.
