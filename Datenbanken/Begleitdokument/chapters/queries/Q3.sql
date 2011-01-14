SELECT Name
FROM Wahlkreis
WHERE ID = 185


SELECT (1.0 * sum(w2.Anzahl) / max(wd.AnzahlWahlberechtigte)) as Wahlbeteiligung
FROM WahlkreisDaten wd, zweitStimmenNachWahlkreis w2
WHERE wd.WahlkreisID = w2.WahlkreisID 
	AND wd.Jahr = w2.jahr 
	AND wd.Jahr = 2009 
	AND wd.WahlkreisID = 185
GROUP BY w2.WahlkreisID


WITH 

ErstStimmenEinWahlkreis AS (
	SELECT *
	FROM erstStimmenNachWahlkreis
	WHERE WahlkreisID=185), 

MaxErststimmenNachWahlkreis AS (
	SELECT k.DMWahlkreisID AS WahlkreisID, MAX(v.Anzahl) AS
		MaxStimmen
	FROM ErstStimmenEinWahlkreis v, Kandidat k
	WHERE v.KandidatID = k.ID 
		AND v.Jahr = 2009
	GROUP BY k.DMWahlkreisID), 

Direktmandate AS (
	SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
	FROM MaxErststimmenNachWahlkreis e, ErstStimmenEinWahlkreis
		v, Kandidat k
	WHERE e.wahlkreisID = v.WahlkreisID 
		AND e.maxStimmen = v.Anzahl 
		AND k.ID = v.KandidatID 
		AND v.Jahr = 2009)
SELECT k.Vorname, k.Nachname, p.Kuerzel
FROM Direktmandate dm, Kandidat k, Partei p
WHERE dm.KandidatID = k.ID 
	AND dm.ParteiID = p.ID 
	AND dm.DMWahlkreisID = 185 

WITH ZweitStimmenWahlkreis2009 AS (
	SELECT ParteiID, Anzahl
	FROM zweitStimmenNachWahlkreis
	WHERE WahlkreisID = 185 
		AND Jahr = 2009), ZweitStimmenWahlkreis2005 AS (
	SELECT ParteiID, Anzahl
	FROM zweitStimmenNachWahlkreis
	WHERE WahlkreisID = 185 
		AND Jahr = 2005), SummeZweitStimmenWahlkreis2009 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM zweitStimmenNachWahlkreis
	WHERE WahlkreisID = 185 
		AND Jahr = 2009
	GROUP BY WahlkreisID ), SummeZweitStimmenWahlkreis2005 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM zweitStimmenNachWahlkreis
	WHERE WahlkreisID = 185 
		AND Jahr = 2005
	GROUP BY WahlkreisID )
SELECT p.Kuerzel, COALESCE(w2009.Anzahl, 0) AS Absolut2009,
	CAST(COALESCE(w2009.Anzahl, 0) AS FLOAT) / ( SELECT Summe
	FROM SummeZweitStimmenWahlkreis2009) AS Prozentual2009,
	COALESCE(w2005.Anzahl, 0) AS Absolut2005,
	CAST(COALESCE(w2005.Anzahl, 0) AS FLOAT) / ( SELECT Summe
	FROM SummeZweitStimmenWahlkreis2005) AS Prozentual2005, (COALESCE(w2009.Anzahl, 0) - COALESCE(w2005.Anzahl, 0)) as Aenderung
FROM ZweitStimmenWahlkreis2009 w2009 FULL OUTER JOIN
	ZweitStimmenWahlkreis2005 w2005 ON w2009.ParteiID =
	w2005.ParteiID RIGHT OUTER JOIN Partei p ON p.ID
	= w2009.ParteiID
ORDER BY Absolut2009 DESC, Absolut2005 DESC