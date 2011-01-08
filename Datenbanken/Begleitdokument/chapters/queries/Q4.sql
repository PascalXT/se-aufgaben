


INSERT INTO Wahlkreissieger WITH MaxErstStimmen(WahlkreisID, Anzahl) AS (
SELECT we.WahlkreisID, MAX(we.Anzahl)
FROM erstStimmenNachWahlkreis we
WHERE we.Jahr = 2009
GROUP BY WahlkreisID ), MaxZweitStimmen(WahlkreisID, Anzahl) AS (
SELECT we.WahlkreisID, MAX(we.Anzahl)
FROM zweitStimmenNachWahlkreis we
WHERE we.Jahr = 2009
GROUP BY WahlkreisID ), GewinnerErstStimmen(WahlkreisID, KandidatID) AS (
SELECT we.WahlkreisID, we.KandidatID
FROM erstStimmenNachWahlkreis we, MaxErstStimmen ms
WHERE we.WahlkreisID = ms.WahlkreisID AND we.Anzahl = ms.Anzahl AND we.Jahr = 2009 ), GewinnerZweitStimmen(WahlkreisID, ParteiID) AS (
SELECT we.WahlkreisID, we.ParteiID
FROM zweitStimmenNachWahlkreis we, MaxZweitStimmen ms
WHERE we.WahlkreisID = ms.WahlkreisID AND we.Anzahl = ms.Anzahl AND we.Jahr = 2009 )
SELECT g1.WahlkreisID, wk.BundeslandID, p1.Kuerzel AS P1, p2.Kuerzel AS P2
FROM GewinnerErstStimmen g1, GewinnerZweitStimmen g2, Partei p1, Partei p2, Kandidat k, Wahlkreis wk
WHERE g1.WahlkreisID = g2.WahlkreisID AND g1.KandidatID = k.ID AND k.ParteiID = p1.ID AND g2.ParteiID = p2.ID AND wk.ID = g1.WahlkreisID 