


INSERT INTO TempErstStimmenNachWahlkreis(KandidatID, WahlkreisID, Jahr, Anzahl)
SELECT s.KandidatID, s.WahlkreisID, Jahr, COUNT(*)
FROM Stimme s
WHERE KandidatID is not null
GROUP BY s.KandidatID, s.WahlkreisID, s.Jahr


INSERT INTO TempZweitStimmenNachWahlkreis(ParteiID, WahlkreisID, Jahr, Anzahl)
SELECT s.ParteiID, s.WahlkreisID, Jahr, COUNT(*)
FROM Stimme s
WHERE ParteiID is not null
GROUP BY s.ParteiID, s.WahlkreisID, s.Jahr


INSERT INTO Direktmandate WITH maxErgebnis(wahlkreisID, maxStimmen) AS (
SELECT k.DMWahlkreisID, MAX(v.Anzahl)
FROM TempErstStimmenNachWahlkreis v, Kandidat k
WHERE v.KandidatID = k.ID AND v.Jahr = 2009
GROUP BY k.DMWahlkreisID )
SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
FROM maxErgebnis e, TempErstStimmenNachWahlkreis v, Kandidat k
WHERE e.wahlkreisID = v.WahlkreisID AND e.maxStimmen = v.Anzahl AND k.ID = v.KandidatID AND v.Jahr = 2009