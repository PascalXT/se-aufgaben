
-- Parteien mit mind. 5 Prozent aller (Zweit-)Stimmen
WITH 
TotalVotes AS (SELECT SUM(v.Anzahl) AS tv FROM Wahlergebnis2 v), 
FivePercentParties AS (SELECT p.ID FROM Partei p, Wahlergebnis2 v WHERE v.ParteiID = p.ID GROUP BY p.ID 
HAVING CAST(SUM(v.Anzahl) AS FLOAT) / (SELECT tv FROM TotalVotes) >= 0.05),

-- Direktmandate (Wahlkreis-Gewinner)
Direktmandate AS (SELECT k.ID, k.ParteiID  FROM Wahlergebnis1 v, Kandidat k WHERE v.KandidatID = k.ID AND v.WahlkreisID = k.DMWahlkreisID 
AND NOT EXISTS (SELECT * FROM Wahlergebnis1 v0 WHERE v0.WahlkreisID = v.WahlkreisID AND v0.Anzahl > v.Anzahl)),

-- Wahlgewinner (Parteien die mind. 5 Prozent der (Zweit-)Stimmen erhalten haben oder mindestens 3 Direktmandate besitzen)
WinnerParties AS (SELECT p.ID FROM Partei p, Kandidat k, Wahlkreis w WHERE k.ParteiID = p.ID AND k.DMWahlkreisID = w.ID AND
(SELECT COUNT(*) FROM Direktmandate d WHERE d.ParteiID = p.ID) >= 3
UNION SELECT * FROM FivePercentParties)

-- erfolgreiche Einzelbewerber
SELECT d.ID, d.ParteiID FROM Direktmandate d WHERE d.ParteiID is NULL OR d.ParteiID NOT IN (SELECT * FROM WinnerParties);
	

