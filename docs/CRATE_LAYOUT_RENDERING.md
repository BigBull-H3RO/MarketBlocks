## MarketCrate Layout-Rendering-Logik

Die neue Rendering-Logik für MarketCrate unterstützt drei verschiedene Layout-Modi:

### 1. STACK-Modus
```
// Vertikale Anordnung der Items übereinander
Für jedes Item i von 0 bis displayCount:
    offsetX = 0
    offsetY = i * offerItemSpacing    // Vertikaler Abstand zwischen Items
    offsetZ = 0
    
Position des Items = BasePosition + Rotation(baseRotation) + (offsetX, offsetY, offsetZ)
```

**Verhalten:** Items werden von unten nach oben gestapelt. Der `offerItemSpacing` Wert bestimmt den vertikalen Abstand zwischen den Items. Bei 0.0 würden sich alle Items überlagern, bei 0.5 wäre der Abstand größer.

### 2. GRID-Modus
```
// Rechteckige Anordnung in XZ-Ebene
gridSize = ceil(sqrt(displayCount))  // Quadratwurzel für quadratisches Raster

Für jedes Item i von 0 bis displayCount:
    x = i % gridSize                  // Position in X
    z = i / gridSize                  // Position in Z
    
    offsetX = (x - (gridSize - 1) / 2.0) * offerItemSpacing  // Zentriert um Mittelpunkt
    offsetY = 0
    offsetZ = (z - (gridSize - 1) / 2.0) * offerItemSpacing  // Zentriert um Mittelpunkt
    
Position des Items = BasePosition + Rotation(baseRotation) + (offsetX, offsetY, offsetZ)
```

**Verhalten:** Items werden in einem rechteckigen Raster angeordnet. Beispiel: Bei 4 Items würde ein 2x2 Raster entstehen. Der `offerItemSpacing` Wert bestimmt den Abstand zwischen den Reihen und Spalten. Die Anordnung wird um den Mittelpunkt zentriert.

### 3. CHAOS-Modus
```
// Zufällige Anordnung in XZ-Ebene
Für jedes Item i von 0 bis displayCount:
    offsetX = (random(-0.5, 0.5)) * offerItemSpacing * 2
    offsetY = 0
    offsetZ = (random(-0.5, 0.5)) * offerItemSpacing * 2
    
Position des Items = BasePosition + Rotation(baseRotation) + (offsetX, offsetY, offsetZ)
```

**Verhalten:** Items werden zufällig in X- und Z-Richtung um die Basis-Position verteilt. Der `offerItemSpacing` Wert bestimmt den maximalen Abstand vom Mittelpunkt. Jedes Mal, wenn der Block geladen wird, wird die gleiche Verteilung durch die Block-Position als Seed erzeugt.

### Allgemeine Rotations-Logik

Vor der Berechnung der Layout-spezifischen Offsets wird die gesamte Item-Gruppe mit `offerItemRotation` (0-360 Grad, Y-Achse) gedreht. Dies bedeutet:

```
PoseStack:
    1. translate(basePosition.x, basePosition.y + heightOffset, basePosition.z)
    2. mulPose(Axis.YP.rotationDegrees(baseRotation))    // Alle Items zusammen drehen
    3. translate(layoutModeOffset.x, layoutModeOffset.y, layoutModeOffset.z)
    4. applySlotRotation(...)  // Individuelle Slot-Rotation
    5. scale(...)
    6. render item
```

### Parameters

- `offerItemCount`: Anzahl der angezeigten Items (0-64). Wird ignoriert, wenn `dynamicFillLevel` aktiv ist.
- `offerItemRotation`: Basis-Rotation der Item-Gruppe (0-360 Grad, Y-Achse)
- `offerItemLayoutMode`: Einer der drei Modi - STACK, GRID oder CHAOS
- `offerItemSpacing`: Abstandswert (0.0-1.0), dessen Effekt je nach Layout-Modus unterschiedlich ist
- `offerItemHeightOffset`: Vertikale Verschiebung der gesamten Item-Gruppe
- `dynamicFillLevel`: Wenn aktiv, wird die Item-Anzahl basierend auf dem Lagerbestand berechnet

### Beispiel-Werte

#### STACK-Modus
- `offerItemSpacing = 0.1`: Items sind dicht gestapelt
- `offerItemSpacing = 0.5`: Items sind weiter auseinander
- Ideal für Höhe-Effekte, um anzuzeigen, wie viel vorrätig ist

#### GRID-Modus  
- `offerItemSpacing = 0.2`: 2x2 Raster mit engen Abständen
- `offerItemSpacing = 0.5`: Items sind weiter auseinander
- Ideal für statische, ordnete Anzeige

#### CHAOS-Modus
- `offerItemSpacing = 0.1`: Items leicht durch- einander
- `offerItemSpacing = 0.5`: Starke Verstreuung
- Ideal für lebendige, chaotische Effekte

