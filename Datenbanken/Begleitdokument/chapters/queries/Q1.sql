INSERT INTO ZweitStimmenNachBundesland
SELECT w2.ParteiID, wk.BundeslandID, sum(w2.Anzahl) as AnzahlStimmen
FROM zweitStimmenNachWahlkreis w2, Wahlkreis wk
WHERE w2.WahlkreisID = wk.ID AND w2.Jahr = 2009
GROUP BY wk.BundeslandID, w2.ParteiID


INSERT INTO ZweitStimmenNachPartei
SELECT ParteiID, SUM(AnzahlStimmen)
FROM ZweitStimmenNachBundesland
GROUP BY ParteiID


INSERT INTO Direktmandate WITH maxErgebnis(wahlkreisID, maxStimmen) AS (
SELECT k.DMWahlkreisID, MAX(v.Anzahl)
FROM erstStimmenNachWahlkreis v, Kandidat k
WHERE v.KandidatID = k.ID AND v.Jahr = 2009
GROUP BY k.DMWahlkreisID )
SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
FROM maxErgebnis e, erstStimmenNachWahlkreis v, Kandidat k
WHERE e.wahlkreisID = v.WahlkreisID AND e.maxStimmen = v.Anzahl AND k.ID = v.KandidatID AND v.Jahr = 2009


INSERT INTO FuenfProzentParteien
SELECT p.ID as ParteiID
FROM Partei p, zweitStimmenNachWahlkreis v
WHERE v.ParteiID = p.ID AND v.Jahr=2009
GROUP BY p.ID
HAVING CAST(SUM(v.Anzahl) AS FLOAT) / (SELECT SUM(AnzahlStimmen)
FROM ZweitStimmenNachBundesland) >= 0.05


INSERT INTO DreiDirektMandatParteien
SELECT dm.ParteiID
FROM Direktmandate dm 
GROUP BY dm.ParteiID
HAVING COUNT(*) >= 3


INSERT INTO ParteienImBundestag
SELECT *
FROM FuenfProzentParteien
UNION 
SELECT *
FROM DreiDirektMandatParteien


INSERT INTO SitzeNachPartei WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5
FROM Wahlkreis w
UNION
SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*)
FROM Wahlkreis) - 0.5
FROM Wahlkreis w), Zugriffsreihenfolge (ParteiID, AnzahlStimmen, DivWert, Rang) as (SELECT p.ParteiID, z.AnzahlStimmen, (z.AnzahlStimmen / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z.AnzahlStimmen / d.wert) DESC) as Rang
FROM ParteienImBundestag p, ZweitStimmenNachPartei z, Divisoren d
WHERE p.ParteiID = z.ParteiID ORDER BY DivWert desc)
SELECT ParteiID, COUNT(Rang) as AnzahlSitze
FROM Zugriffsreihenfolge 
WHERE Rang <= 598 
GROUP BY ParteiID


INSERT INTO SitzeNachLandeslisten WITH Divisoren (wert) as (
SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5
FROM Wahlkreis w
UNION
SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*)
FROM Wahlkreis) - 0.5
FROM Wahlkreis w ), Zugriffsreihenfolge (ParteiID, BundeslandID, AnzahlStimmen, DivWert, Rang) as (SELECT p.ParteiID, z.BundeslandID, z.AnzahlStimmen, (z.AnzahlStimmen / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p.ParteiID ORDER BY (z.AnzahlStimmen / d.wert) DESC) as Rang
FROM ParteienImBundestag p, ZweitStimmenNachBundesland z, Divisoren d
WHERE p.ParteiID = z.ParteiID ORDER BY ParteiID, DivWert desc) 
SELECT z.ParteiID, BundeslandID, COUNT(Rang) as AnzahlSitze
FROM Zugriffsreihenfolge z, SitzeNachPartei s
WHERE z.ParteiID = s.ParteiID AND z.Rang <= s.AnzahlSitze
GROUP BY z.ParteiID, z.BundeslandID, s.ParteiID


INSERT INTO Ueberhangsmandate WITH DirektMandateProParteiUndBundesland AS (SELECT k.ParteiID, w.BundeslandID, COUNT(*) AS AnzahlDirektmandate
FROM Direktmandate dm, Kandidat k, Wahlkreis w
WHERE dm.KandidatID = k.ID AND w.ID = k.DMWahlkreisID
GROUP BY k.ParteiID, w.BundeslandID)
SELECT b.ID AS BundeslandID, b.Name, p.ID AS ParteiID, p.Kuerzel, dmpb.AnzahlDirektmandate - s.AnzahlSitze AS AnzahlUeberhangsmandate
FROM DirektMandateProParteiUndBundesland dmpb, SitzeNachLandeslisten s, Partei p, Bundesland b
WHERE dmpb.BundeslandID = s.BundeslandID AND dmpb.ParteiID = s.ParteiID AND dmpb.ParteiID = p.ID AND dmpb.BundeslandID = b.ID AND dmpb.AnzahlDirektmandate - s.AnzahlSitze > 0