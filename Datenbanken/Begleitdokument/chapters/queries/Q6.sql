


WITH MaxStimmen(WahlkreisID, Anzahl) AS (
SELECT we.WahlkreisID, MAX(we.Anzahl)
FROM erstStimmenNachWahlkreis we
WHERE we.Jahr = 2009
GROUP BY we.WahlkreisID ), Erster(WahlkreisID, KandidatID, ParteiID, Anzahl) AS (
SELECT we.WahlkreisID, we.KandidatID, k.ParteiID, we.Anzahl
FROM erstStimmenNachWahlkreis we, MaxStimmen ms, Kandidat k
WHERE we.WahlkreisID = ms.WahlkreisID AND we.Jahr = 2009 AND we.KandidatID = k.ID AND we.Anzahl = ms.Anzahl ), RestKandidaten(KandidatID) AS (
SELECT KandidatID
FROM erstStimmenNachWahlkreis we
WHERE we.Jahr = 2009 EXCEPT
SELECT KandidatID
FROM Erster ), MaxStimmenRest(WahlkreisID, Anzahl) AS (
SELECT we.WahlkreisID, MAX(we.Anzahl)
FROM erstStimmenNachWahlkreis we, RestKandidaten r
WHERE we.KandidatID = r.KandidatID AND we.Jahr = 2009
GROUP BY we.WahlkreisID ), Zweiter(WahlkreisID, KandidatID, ParteiID, Anzahl) AS (
SELECT k.DMWahlkreisID, k.ID, k.ParteiID, we.Anzahl
FROM Kandidat k, erstStimmenNachWahlkreis we, MaxStimmenRest ms
WHERE we.Anzahl = ms.Anzahl AND we.WahlkreisID = ms.WahlkreisID AND we.KandidatID = k.ID AND we.Jahr = 2009 ), KnappsteSieger(GewinnerID, Differenz, VerliererID, WahlkreisID) AS (
SELECT e.ParteiID, e.Anzahl - z.Anzahl AS Differenz, z.ParteiID, e.WahlkreisID
FROM Erster e, Zweiter z
WHERE e.WahlkreisID = z.WahlkreisID ORDER BY e.ParteiID, Differenz ), KnappsteSiegerRang(Rang, GewinnerID, Differenz, VerliererID, WahlkreisID) AS (
SELECT ROW_NUMBER() OVER (PARTITION BY kn.GewinnerID ORDER BY kn.Differenz), kn.GewinnerID, kn.Differenz, kn.VerliererID, kn.WahlkreisID
FROM KnappsteSieger kn ), ParteienOhneSieg(ParteiID) AS (
SELECT ID
FROM Partei EXCEPT
SELECT GewinnerID
FROM KnappsteSiegerRang ), KnappsteSiegerOutput(Rang, Vorname, Nachname, Partei, Wahlkreis, Differenz, Typ) AS (
SELECT knr.Rang, k.Vorname, k.Nachname, p.Kuerzel, wk.Name, knr.Differenz, 'Gewinner'
FROM KnappsteSiegerRang knr, Partei p, Kandidat k, Wahlkreis wk
WHERE Rang <= 10 AND knr.GewinnerID = p.ID AND knr.WahlkreisID = k.DMWahlkreisID AND k.ParteiID = p.ID AND wk.ID = knr.WahlkreisID ), KnappsteVerlierer(ParteiID, KandidatID, AbstandZumErsten, WahlkreisID) AS (
SELECT pos.ParteiID, k.ID, e.Anzahl - we.Anzahl, w.ID
FROM ParteienOhneSieg pos, Erster e, Wahlkreis w, erstStimmenNachWahlkreis we, Kandidat k
WHERE we.WahlkreisID = w.ID AND we.Jahr = 2009 AND e.WahlkreisID = w.ID AND we.KandidatID = k.ID AND k.DMWahlkreisID = w.ID AND k.ParteiID = pos.ParteiID ), KnappsteVerliererRang(Rang, ParteiID, KandidatID, AbstandZumErsten, WahlkreisID) AS (
SELECT ROW_NUMBER() OVER (PARTITION BY kv.ParteiID ORDER BY kv.AbstandZumErsten ASC), kv.ParteiID, kv.KandidatID, kv.AbstandZumErsten, kv.WahlkreisID
FROM KnappsteVerlierer kv ), KnappsteVerliererOutput(Rang, Vorname, Nachname, Partei, Wahlkreis, Differenz, Typ) AS (
SELECT kvr.Rang, k.Vorname, k.Nachname, p.Kuerzel, wk.Name, kvr.AbstandZumErsten, 'Verlierer'
FROM KnappsteVerliererRang kvr, Partei p, Kandidat k, Wahlkreis wk
WHERE Rang <= 10 AND kvr.ParteiID = p.ID AND kvr.WahlkreisID = k.DMWahlkreisID AND k.ParteiID = p.ID AND wk.ID = kvr.WahlkreisID ), GewinnerUndVerliererOutput AS (
SELECT *
FROM KnappsteSiegerOutput
UNION ALL
SELECT *
FROM KnappsteVerliererOutput )
SELECT *
FROM GewinnerUndVerliererOutput ORDER BY Typ