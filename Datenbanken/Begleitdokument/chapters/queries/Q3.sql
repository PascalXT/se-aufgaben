


INSERT INTO Direktmandate WITH maxErgebnis(wahlkreisID, maxStimmen) AS (
SELECT k.DMWahlkreisID, MAX(v.Anzahl)
FROM erstStimmenNachWahlkreis v, Kandidat k
WHERE v.KandidatID = k.ID AND v.Jahr = 2009
GROUP BY k.DMWahlkreisID )
SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
FROM maxErgebnis e, erstStimmenNachWahlkreis v, Kandidat k
WHERE e.wahlkreisID = v.WahlkreisID AND e.maxStimmen = v.Anzahl AND k.ID = v.KandidatID AND v.Jahr = 2009