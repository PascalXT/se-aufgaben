SELECT Name
FROM Wahlkreis
WHERE ID = 215

SELECT (1.0 * sum(w2.Anzahl) / max(wd.AnzahlWahlberechtigte)) as Wahlbeteiligung
FROM WahlkreisDaten wd, ZweitStimmenNachWahlkreis w2
WHERE wd.WahlkreisID = w2.WahlkreisID 
	AND wd.Jahr = w2.jahr 
	AND wd.Jahr = 2009 
	AND wd.WahlkreisID = 215
GROUP BY w2.WahlkreisID

WITH 

ErstStimmenEinWahlkreis AS (
	SELECT *
	FROM ErstStimmenNachWahlkreis
	WHERE WahlkreisID=215), 

MaxErststimmenNachWahlkreis AS (
	SELECT v.WahlkreisID, MAX(v.Anzahl) AS MaxStimmen
	FROM ErstStimmenEinWahlkreis v
	WHERE v.Jahr = 2009
	GROUP BY v.WahlkreisID), 

DirektmandateNummer AS (
	SELECT e.KandidatID, e.WahlkreisID, Row_Number() OVER
		(PARTITION BY e.WahlkreisID) AS Nummer
	FROM MaxErststimmenNachWahlkreis m,
		ErstStimmenNachWahlkreis e
	WHERE e.WahlkreisID = m.WahlkreisID 
		AND	m.MaxStimmen = e.Anzahl 
		AND e.Jahr = 2009), 

DirektmandateMaxNummer AS (
	SELECT WahlkreisID, MAX(Nummer) AS kMaxNummer
	FROM DirektmandateNummer
	GROUP BY WahlkreisID), 

Direktmandate AS (
	SELECT n.KandidatID, k.ParteiID, k.DMWahlkreisID
	FROM DirektmandateNummer n, DirektmandateMaxNummer mn,
		ZufallsZahlenDirektmandate z, Kandidat k
	WHERE n.WahlkreisID = mn.WahlkreisID 
		AND k.ID = n.KandidatID 
		AND z.Zeile = mod(n.WahlkreisID, ( SELECT COUNT(*) FROM
			ZufallsZahlenDirektmandate)) 
		AND n.Nummer = mod(z.Zahl, mn.kMaxNummer) + 1)
SELECT k.Vorname, k.Nachname, p.Kuerzel
FROM Direktmandate dm, Kandidat k, Partei p
WHERE dm.KandidatID = k.ID 
	AND dm.ParteiID = p.ID 
	AND dm.DMWahlkreisID = 215 

WITH ZweitStimmenWahlkreis2009 AS (
	SELECT ParteiID, Anzahl
	FROM ZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 215 
		AND Jahr = 2009), ZweitStimmenWahlkreis2005 AS (
	SELECT ParteiID, Anzahl
	FROM ZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 215 
		AND Jahr = 2005), SummeZweitStimmenWahlkreis2009 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM ZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 215 
		AND Jahr = 2009
	GROUP BY WahlkreisID ), SummeZweitStimmenWahlkreis2005 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM ZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 215 
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

